require_relative '../../../../../lib/post_provision_test'


class CapSetuidPython3Test < PostProvisionTest
  def initialize
    self.module_name = 'cap_setuid_python3'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # Inputs: leaked_filenames and strings_to_leak are both checked below.
  def test_module
    super
    leaked = Array(self.json_inputs['leaked_filenames']).map(&:to_s).reject(&:empty?)
    strings = Array(self.json_inputs['strings_to_leak']).map(&:to_s)

    # The capability is on the versioned binary, not the /usr/bin/python3 symlink
    real = '$(readlink -f /usr/bin/python3)'
    getcap = 'PATH=$PATH:/usr/sbin:/sbin getcap'

    tier(1) do
      test_local_command('python3 binary has cap_setuid=ep', "#{getcap} #{real}", 'cap_setuid=ep')
      test_local_command('capability is on a versioned binary, not the symlink',
                         "readlink -f /usr/bin/python3", '/usr/bin/python3.')

      leaked.each do |f|
        test_local_command("#{f} root-only in /root", "stat -c '%U:%G %a' #{Shellwords.escape("/root/#{f}")}", 'root:root 600')
      end
    end

    tier(2) do
      # what a student finds by enumerating: getcap -r shows the capability
      test_local_command('unprivileged user finds cap_setuid with getcap -r',
                         "#{getcap} -r /usr/bin /usr/local/bin 2>/dev/null", 'cap_setuid=ep', user: 'nobody')
      # the flag is not readable without escalating
      strings.each_with_index do |s, i|
        next if s.strip.empty?
        fname = leaked[i] || leaked.first
        next unless fname
        test_command_succeeds("unprivileged user cannot read leaked #{fname} directly",
                              "! cat #{Shellwords.escape("/root/#{fname}")} 2>/dev/null", user: 'nobody')
      end
    end

    # TODO tier 3 (exploit): hand-written — with_throwaway_ssh_user, then as that user run the GTFOBins python3 "capabilities" technique (os.setuid(0)) to read the root-only flag; remove nothing else (helper deletes the user)
  end

end

CapSetuidPython3Test.new.run
