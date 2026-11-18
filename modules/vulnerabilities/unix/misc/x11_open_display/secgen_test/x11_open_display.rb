require_relative '../../../../../lib/post_provision_test'

# Tiers 1-2 for the open X11 display module. Tier 3 (driving the session over the
# network to run a command as the logged-in user) is a Metasploit exploit and is
# deferred to the t3-worker; the stub at the end of test_module names the module.
#
# read_facts and how each is checked:
#   desktop_user      - the autologin account; checked (account exists, owns the
#                       flag, named in the SDDM drop-in).
#   desktop_password  - sets that account's password; checked via the SHA-512
#                       hash (fixed salt) in /etc/shadow.
#   leaked_filenames  - the flag's filename in the user's home; checked (exists,
#                       owned by the user).
#   strings_to_leak   - the flag's contents; checked (file content matches).
#
# The X display is :0, which is always TCP 6000 - not a parameterised input, so
# it is a literal here.
class X11OpenDisplayTest < PostProvisionTest
  DROPIN = '/etc/sddm.conf.d/zz-x11-open-display.conf'.freeze
  X11_PORT = 6000

  def initialize
    self.module_name = 'x11_open_display'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super

    desktop_user = self.json_inputs['desktop_user']&.first
    desktop_pass = self.json_inputs['desktop_password']&.first
    flag_name    = self.json_inputs['leaked_filenames']&.first
    flag_body    = self.json_inputs['strings_to_leak']&.first
    skip!('desktop_user not resolved in json_inputs') unless desktop_user

    tier(1) do
      # xterm is what the exploit opens via KRunner; the base lacks it.
      test_local_command('xterm installed', 'test -x /usr/bin/xterm && echo ok', 'ok')

      # The SDDM drop-in is present and opens the X server to the network.
      test_local_command('sddm drop-in present', "test -f #{DROPIN} && echo ok", 'ok')
      test_local_command('drop-in enables TCP', "grep -- '-listen tcp' #{DROPIN}", '-listen tcp')
      test_local_command('drop-in disables access control', "grep -- '-ac' #{DROPIN}", '-ac')
      test_local_command('drop-in autologs in the desktop user',
                         "grep -E '^User=#{Regexp.escape(desktop_user)}$' #{DROPIN}", "User=#{desktop_user}")

      # The non-privileged desktop account exists.
      test_local_command('desktop user exists', "getent passwd #{desktop_user}", "#{desktop_user}:")

      # The flag file exists in the user's home, owned by that user.
      if flag_name
        test_local_command('flag present in home',
                           "stat -c '%U' /home/#{desktop_user}/#{flag_name}", desktop_user)
      end
    end

    tier(2) do
      # X is listening on TCP 6000 and the running server has access control off.
      test_local_command('X listening on TCP 6000', 'ss -ltn', ":#{X11_PORT} ")
      xorg = run_command('ps -eo args | grep -E "[X]org|[/ ]X " | head -1')
      args = xorg[:stdout].to_s
      if args.include?('-listen tcp') && args.include?('-ac')
        pass_check('X server started open', "Xorg running with -listen tcp -ac: #{args.strip}", tier: 2)
      else
        fail_check('X server started open',
                   "Xorg not running with -listen tcp -ac (got: #{args.strip.empty? ? 'no Xorg process' : args.strip})",
                   tier: 2, evidence: 'ps -eo args | grep -i xorg')
      end

      # The desktop_password input really set this account's password.
      if desktop_pass
        expected = desktop_pass.crypt('$6$secgenx11$')
        shadow = run_command("getent shadow #{desktop_user}")
        if shadow[:stdout].to_s.include?(expected)
          pass_check('desktop password set', "account #{desktop_user} has the configured password hash", tier: 2)
        else
          fail_check('desktop password set',
                     "account #{desktop_user} shadow entry does not match the configured password",
                     tier: 2, evidence: "getent shadow #{desktop_user}")
        end
      end

      # The flag's contents match the configured strings_to_leak.
      if flag_name && flag_body
        content = run_command("cat /home/#{desktop_user}/#{flag_name}")
        if content[:stdout].to_s.include?(flag_body)
          pass_check('flag contents match', "flag file holds the configured leaked string", tier: 2)
        else
          fail_check('flag contents match', 'flag file content does not match strings_to_leak',
                     tier: 2, evidence: "cat /home/#{desktop_user}/#{flag_name}")
        end
      end
    end

    # The exploit drives the GUI (Alt+F2 -> KRunner -> xterm, then types the
    # payload), with TIME_WAIT pauses, and runs a no-session command payload.
    # The typed command finishes executing in xterm after the module returns, so
    # poll for the sentinel rather than reading it immediately.
    #
    # TIME_WAIT is the module's pause (default 5s) after Alt+F2 for KRunner to
    # appear and after typing "xterm" for the terminal to open. On a freshly
    # booted Plasma VM 5s is too short - the keystrokes land before the window
    # exists and the payload is lost, so the sentinel never appears. Raise it so
    # both windows are ready before the module types into them.
    test_msf_exploit('exploit/unix/x11/x11_keyboard_exec',
                     rport: self.port,
                     expect_privilege: 'user_rwx',
                     options: { 'TIME_WAIT' => 20 },
                     sentinel_wait: 60,
                     tier: 3)
  end
end

X11OpenDisplayTest.new.run
