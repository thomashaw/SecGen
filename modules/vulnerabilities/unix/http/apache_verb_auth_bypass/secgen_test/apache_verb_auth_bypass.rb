require_relative '../../../../../lib/post_provision_test'
require 'base64'
require 'shellwords'

class ApacheVerbAuthBypassTest < PostProvisionTest
  # The apache module serves the misconfigured vhost on port 80; not an input.
  PORT = 80
  # Directory protected with <Limit GET POST> (see manifests/config.pp).
  PROTECTED_PATH = '/private/'.freeze

  def initialize
    self.module_name = 'apache_verb_auth_bypass'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    # Inputs (see secgen_metadata.xml). All four read_facts are observable:
    #   unix_username / used_password — the Basic Auth credentials (tier 2 below)
    #   leaked_filenames             — the protected secret's filename
    #   strings_to_leak              — the secret's content
    username = self.json_inputs['unix_username']&.first.to_s
    password = self.json_inputs['used_password']&.first.to_s
    filename = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?).first
    secret_line = self.json_inputs['strings_to_leak']&.first.to_s
                      .lines.map(&:strip).reject(&:empty?).first

    tier(1) do
      test_local_command('apache2 installed?', 'dpkg -l apache2 | grep -E "^ii"', 'apache2')
      # The misconfiguration itself is on disk: <Limit GET POST> guarding the dir.
      test_local_command('<Limit GET POST> config present?',
                         "grep -rh 'Limit GET POST' /etc/apache2/ 2>/dev/null",
                         'Limit GET POST')
      test_local_command('htpasswd file present?',
                         'test -f /etc/apache2/.htpasswd && echo present', 'present')
      if filename
        test_local_command('protected secret file present?',
                           "test -f /var/www/html/private/#{Shellwords.escape(filename)} && echo present",
                           'present')
      else
        skip_check('protected secret file present?', 'no leaked_filenames input', tier: 1)
      end
    end

    tier(2) do
      test_service_up(port: PORT)

      # 1. A GET of the protected directory is challenged for credentials (401).
      get = raw_http('GET', PROTECTED_PATH)
      if get[:status] == '401' && get[:raw].include?('WWW-Authenticate')
        pass_check('GET requires auth',
                   "GET #{PROTECTED_PATH} returned 401 with a WWW-Authenticate challenge", tier: 2)
      else
        fail_check('GET requires auth',
                   "GET #{PROTECTED_PATH} returned #{get[:status].inspect}, expected a 401 Basic Auth challenge",
                   tier: 2, evidence: get[:raw][0, 400])
      end

      # 2. The configured credentials unlock the protected secret — proves the
      #    username/password inputs and the placed strings_to_leak content.
      if !username.empty? && !password.empty? && filename && secret_line && !secret_line.empty?
        token = Base64.strict_encode64("#{username}:#{password}")
        authed = raw_http('GET', "/private/#{filename}", auth: token)
        if authed[:status] == '200' && authed[:raw].include?(secret_line)
          pass_check('valid credentials return the secret',
                     "GET /private/#{filename} with the configured credentials returned the leaked string", tier: 2)
        else
          fail_check('valid credentials return the secret',
                     "GET /private/#{filename} with the configured credentials returned #{authed[:status].inspect} and did not contain the leaked string",
                     tier: 2, evidence: authed[:raw][0, 400])
        end
      else
        skip_check('valid credentials return the secret',
                   'missing unix_username/used_password/leaked_filenames/strings_to_leak input', tier: 2)
      end

      # 3. The misconfiguration: a verb outside <Limit GET POST> (TRACE) is served
      #    without credentials. This is exactly what the tier-3 MSF auxiliary
      #    (verb_auth_bypass) detects — a verb whose status differs from the GET's
      #    401. A <LimitExcept GET POST> fix would return 401 here too.
      trace = raw_http('TRACE', PROTECTED_PATH)
      if trace[:status] == '200'
        pass_check('verb bypass (TRACE) is unauthenticated',
                   "TRACE #{PROTECTED_PATH} returned 200 without credentials — <Limit GET POST> leaves it unprotected", tier: 2)
      else
        fail_check('verb bypass (TRACE) is unauthenticated',
                   "TRACE #{PROTECTED_PATH} returned #{trace[:status].inspect}, expected 200 (verb auth bypass)",
                   tier: 2, evidence: trace[:raw][0, 400])
      end
    end

    # Tier 3: prove the bypass with the stock MSF auxiliary scanner from the
    # attacker VM. This is an auxiliary/scanner module proving an info leak (not
    # a session exploit), so it is driven with run_msf_module and asserted here
    # rather than via the deferred test_msf_exploit stub / expect_privilege path.
    #
    # The scanner (auxiliary/scanner/http/verb_auth_bypass) first does a baseline
    # GET of TARGETURI and only continues if that GET is challenged with a
    # WWW-Authenticate header; it then retries the path with each of
    # ['HEAD','TRACE','TRACK','Wmap','get','trace'] and reports a bypass for any
    # verb whose status differs from the GET's and is <= 302. TARGETURI MUST be
    # the protected directory — against the docroot it would see no challenge and
    # report nothing (this is what the earlier empty-options generation got
    # wrong). Of those verbs only uppercase TRACE returns 200 here: HEAD folds
    # into GET under <Limit>, and TRACK/Wmap/lowercase verbs draw a 4xx/5xx.
    tier(3) do
      aux = run_msf_module('auxiliary/scanner/http/verb_auth_bypass',
                           options: { 'TARGETURI' => PROTECTED_PATH, 'RPORT' => PORT },
                           label: 'apache_verb_auth_bypass')
      next unless aux

      out = aux[:msf_output].to_s
      # print_good -> "[+] ... Possible authentication bypass with verb TRACE [200]"
      if out =~ /authentication bypass with verb/i
        verb = out[/authentication bypass with verb (\S+)/i, 1]
        pass_check('verb_auth_bypass confirms the bypass',
                   "auxiliary/scanner/http/verb_auth_bypass on #{aux[:attacker]} reported an authentication bypass (verb #{verb}) against #{aux[:rhost]}#{PROTECTED_PATH}",
                   tier: 3)
      else
        fail_check('verb_auth_bypass confirms the bypass',
                   'No "authentication bypass with verb" finding in the scanner output; see the msf_output evidence.',
                   tier: 3)
      end
    end
  end

  private

  # Send one raw HTTP/1.0 request over /dev/tcp from inside the guest, so no HTTP
  # client (curl/wget) need be installed and any verb or header can be sent. The
  # request and the bash runner are passed as separate shell-escaped arguments,
  # the same pattern the base class's guest_http_get uses. Returns {status:, raw:}.
  def raw_http(method, path, auth: nil, host: '127.0.0.1', port: PORT)
    req = +"#{method} #{path} HTTP/1.0\\r\\nHost: #{host}\\r\\n"
    req << "Authorization: Basic #{auth}\\r\\n" if auth
    req << "Connection: close\\r\\n\\r\\n"
    script = "exec 3<>/dev/tcp/#{host}/#{port} && printf %b \"$1\" >&3 && cat <&3"
    cmd = "timeout 20 bash -c #{Shellwords.escape(script)} _ #{Shellwords.escape(req)}"
    out = run_command(cmd)[:stdout].to_s
    { status: out[%r{HTTP/\S+\s+(\d{3})}, 1], raw: out }
  end
end

ApacheVerbAuthBypassTest.new.run
