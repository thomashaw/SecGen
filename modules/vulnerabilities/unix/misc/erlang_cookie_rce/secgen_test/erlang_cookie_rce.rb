require_relative '../../../../../lib/post_provision_test'
require 'shellwords'

# read_facts (all five checked here or justified):
#   unix_username     - the service account; checked at tier 1 (process owner, epmd
#                       node) and tier 2, and is where the flag is leaked.
#   erlang_dist_port  - the pinned distribution listener port; checked at tier 2.
#   cookie            - the guessable distribution cookie; proven accepted at tier 2
#                       by a local Erlang node that pings the target with it.
#   strings_to_leak   - the flag contents; read by the tier-3 exploit (deferred),
#                       so not asserted here (the file is owned 0600 by the service
#                       user and only reachable once a shell as that user is had).
#   leaked_filenames  - the flag filename; same as above, exercised by tier 3.
class ErlangCookieRceTest < PostProvisionTest
  def initialize
    self.module_name = 'erlang_cookie_rce'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    username  = self.json_inputs['unix_username']&.first    || 'erlang'
    cookie    = self.json_inputs['cookie']&.first           || 'secret'
    dist_port = self.json_inputs['erlang_dist_port']&.first || '25672'

    tier(1) do
      test_local_command('erlang runtime installed',
                         'dpkg -l erlang-nox | grep -E "^ii"', 'erlang-nox')
      test_local_command('erl and epmd present',
                         'test -x /usr/bin/erl && test -x /usr/bin/epmd && echo ok', 'ok')
      # The node must run as the unprivileged service user, not root.
      test_local_command('node process owned by the service user',
                         "ps -o user= -C beam.smp | tr -d ' ' | sort -u", username)
      # epmd lists the registered distributed node.
      test_local_command('epmd lists the distributed node',
                         'epmd -names', 'name secgen at port')
      # The guessable cookie file is present in the service user's home, 0400.
      test_local_command('cookie file present and 0400',
                         "stat -c '%a %U' /home/#{username}/.erlang.cookie", "400 #{username}")
    end

    tier(2) do
      # epmd (4369) and the distribution listener must be reachable off loopback,
      # which is how the attacker VM reaches the node at tier 3. Accept any
      # wildcard local address (0.0.0.0, * or [::]).
      test_local_command('epmd listening on all interfaces',
                         "ss -ltnH | awk '{print $4}' | grep -Eq '(0\\.0\\.0\\.0|\\*|\\[::\\]):4369$' && echo OPEN", 'OPEN')
      test_local_command('distribution listener on all interfaces',
                         "ss -ltnH | awk '{print $4}' | grep -Eq '(0\\.0\\.0\\.0|\\*|\\[::\\]):#{dist_port}$' && echo OPEN", 'OPEN')

      # Prove the cookie is accepted: a throwaway local Erlang node that presents
      # the configured cookie gets 'pong' from the target (a wrong cookie returns
      # 'pang'). This is exactly the authentication the exploit relies on.
      probe = "erl -sname probe$$ -setcookie #{Shellwords.escape(cookie)} -noshell " \
              "-eval 'case net_adm:ping(list_to_atom(\"secgen@\"++net_adm:localhost())) of " \
              "pong -> io:format(\"COOKIE_OK~n\"); Other -> io:format(\"COOKIE_~p~n\",[Other]) end, halt().'"
      test_local_command('weak distribution cookie accepted', probe, 'COOKIE_OK')
    end

    test_msf_exploit('exploit/multi/misc/erlang_cookie_rce',
                     rport: self.port,
                     expect_privilege: 'user_rwx',
                     tier: 3)
  end
end

ErlangCookieRceTest.new.run
