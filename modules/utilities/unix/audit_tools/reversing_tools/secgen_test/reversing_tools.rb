require_relative '../../../../../lib/post_provision_test'

class ReversingToolsTest < PostProvisionTest
  def initialize
    self.module_name = 'reversing_tools'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    osr = run_vagrant_ssh('. /etc/os-release; echo "$ID ${VERSION_ID%%.*}"')[:stdout].split
    kali = osr[0] == 'kali'
    # pwntools, radare2 and GEF are installed on Kali and Debian 12+ only, as in the Puppet
    modern = kali || (osr[0] == 'debian' && osr[1].to_i >= 12)

    tier(1) do
      test_local_command('gdb installed?', 'which gdb', 'gdb')
      %w[gdb git ltrace strace valgrind pax-utils binwalk vbindiff ssdeep gcc-multilib yara nmap].each do |pkg|
        test_local_command("#{pkg} package installed?", "dpkg -l #{pkg} | grep '^ii'", pkg)
      end
      test_local_command('upx installed?', 'test -x /usr/local/bin/upx && echo "ok"', 'ok')
      # procyon-decompiler is skipped on Kali (removed from kali-rolling)
      test_local_command('procyon installed?', 'dpkg -l procyon-decompiler | grep "^ii"', 'procyon') unless kali
      if modern
        test_local_command('pwntools installed?', 'dpkg -l python3-pwntools | grep "^ii"', 'pwntools')
        test_local_command('radare2 installed?', 'which radare2', 'radare2')
        test_local_command('gef script present', 'test -f /opt/gef/gef.py && echo "ok"', 'ok')
        test_local_command('gef launcher present', 'test -x /usr/local/bin/gef && echo "ok"', 'ok')
      end
      test_local_command('cutter AppImage deployed', 'test -x /opt/Cutter/Cutter-v1.7.2-x86_64.Linux.AppImage && echo "ok"', 'ok')
      test_local_command('cutter launcher present', 'test -x /usr/local/bin/cutter && echo "ok"', 'ok')
    end

    tier(2) do
      test_command_succeeds('gdb reports version', 'gdb --version')
      test_command_succeeds('strace available', 'strace --version')
      test_local_command('upx runs', '/usr/local/bin/upx --version 2>&1', 'upx')
      test_local_command('ltrace runs', 'ltrace --version 2>&1', 'ltrace')
      test_local_command('valgrind runs', 'valgrind --version 2>&1', 'valgrind')
      test_local_command('scanelf (pax-utils) runs', 'scanelf -V 2>&1', 'scanelf')
      test_local_command('binwalk runs', 'binwalk --help 2>&1', 'inwalk')
      test_local_command('vbindiff runs', 'vbindiff --help 2>&1; true', 'vbindiff')
      test_local_command('ssdeep runs', 'ssdeep -V 2>&1', '.')
      test_local_command('yara runs', 'yara --version 2>&1', '.')
      test_local_command('nmap runs', 'nmap --version 2>&1', 'Nmap')
      test_local_command('procyon runs', 'procyon-decompiler --help 2>&1', 'rocyon') unless kali
      if modern
        test_local_command('radare2 runs', 'radare2 -v 2>&1', 'radare2')
        test_local_command('pwntools imports', "python3 -c 'import pwn; print(pwn.__name__)' 2>&1", 'pwn')
        # gef loads into gdb and reports its version
        test_local_command('gef loads in gdb', 'gef -batch -ex "gef help" 2>&1', 'GEF')
      end
    end
  end
end

ReversingToolsTest.new.run
