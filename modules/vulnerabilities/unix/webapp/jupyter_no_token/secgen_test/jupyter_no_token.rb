require_relative '../../../../../lib/post_provision_test'

# Post-provision test for the no-authentication Jupyter Notebook module. The
# module installs jupyter-notebook from Debian and runs it under systemd as the
# unprivileged 'jupyter' account with an empty token and password, bound to all
# interfaces on the configured port, with the account's home as the notebook
# directory. A flag (strings_to_leak) is leaked into that home under
# leaked_filenames, so an unauthenticated visitor can list it through the
# contents API and read it (or run code in a Python kernel) as the service user.
#
# read_facts covered:
#   port             -> tier 1 config grep + tier 2 test_service_up.
#   leaked_filenames -> tier 1 file present in the notebook dir + tier 2
#                       unauthenticated /api/contents listing names it.
#   strings_to_leak  -> tier 1 the flag file on the target holds the string.
#
# Tiers:
#   1  jupyter-notebook, the service account, the config and the leaked flag are
#      present on the target.
#   2  the server listens on the configured port and answers /api and
#      /api/contents without any credentials (the misconfiguration itself).
#   3  deferred to the t3-worker: the Metasploit auxiliary scanner confirms no
#      login is required and the flag is read back through the contents API.
class JupyterNoTokenTest < PostProvisionTest
  CONFIG = '/home/jupyter/.jupyter/jupyter_notebook_config.py'.freeze
  HOME   = '/home/jupyter'.freeze

  def initialize
    self.module_name = 'jupyter_no_token'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    port             = self.port
    leaked_filenames = self.json_inputs['leaked_filenames'] || []
    leaked_strings   = self.json_inputs['strings_to_leak'] || []

    # Tier 1 — provisioned on the target.
    tier(1) do
      test_command_succeeds('jupyter-notebook installed', 'test -x /usr/bin/jupyter-notebook')
      test_command_succeeds('jupyter service user exists', 'id jupyter')
      test_command_succeeds('jupyter config present', "test -f #{CONFIG}")
      test_command_succeeds('configured port in config', "grep -q 'port = #{port}' #{CONFIG}") if port

      # The flag file(s) are leaked into the notebook directory (the home).
      leaked_filenames.each do |fname|
        test_command_succeeds("leaked file #{fname} in notebook dir",
                              "test -f #{HOME}/#{fname}")
      end
      # The leaked flag string is actually written into the notebook directory.
      if (flag = leaked_strings.first)
        test_command_succeeds('flag string present in notebook dir',
                              "grep -rqF #{shell_quote(flag)} #{HOME}")
      end
    end

    # Tier 2 — the server answers over HTTP with no credentials. /api returns the
    # version, and /api/contents lists the notebook directory (naming the leaked
    # file) for anyone, which is the misconfiguration this module demonstrates.
    tier(2) do
      test_service_up
      test_http('/api', match: 'version')
      if (fname = leaked_filenames.first)
        test_http('/api/contents', match: fname)
      else
        test_http('/api/contents', status: 200)
      end
    end

    # Tier 3: prove the misconfiguration with the stock MSF auxiliary module.
    # run_msf_module runs it from the attacker VM (RHOSTS set automatically) and
    # returns :msf_output. The assertion below is a generic success heuristic (an
    # MSF "[+]" finding line). TODO t3-worker: tighten it to a module-specific
    # token from the msf_output_jupyter_login evidence, and add any options the module
    # needs (credentials, RPORT) from the module's json_inputs.
    tier(3) do
      aux = run_msf_module('auxiliary/scanner/http/jupyter_login',
                           options: {},
                           label: 'jupyter_no_token')
      next unless aux

      out = aux[:msf_output].to_s
      if out.include?('[+]')
        pass_check('jupyter_no_token proves the misconfiguration',
                   "auxiliary/scanner/http/jupyter_login on #{aux[:attacker]} reported a finding against #{aux[:rhost]}",
                   tier: 3)
      else
        fail_check('jupyter_no_token proves the misconfiguration',
                   'No MSF "[+]" success line in the auxiliary output; see msf_output evidence.',
                   tier: 3)
      end
    end
  end

  # Single-quote a value for safe interpolation into a shell command.
  def shell_quote(value)
    "'" + value.to_s.gsub("'", "'\\''") + "'"
  end
end

JupyterNoTokenTest.new.run
