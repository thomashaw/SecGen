require_relative '../../../../../lib/post_provision_test'

class PcapFileTest < PostProvisionTest
  def initialize
    self.module_name = 'pcap_file'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # A .pcap (or arbitrarily named) file is leaked into a user home or storage directory.
      # The leaked_filename is parameterised, so search broadly.
      test_local_command('leaked file exists', 'find /home /root -mindepth 1 -maxdepth 3 -type f | head -1', '/')
    end
    tier(2) do
      # libpcap files start with magic bytes d4 c3 b2 a1 (LE) or a1 b2 c3 d4 (BE).
      test_local_command('pcap magic bytes present',
        'find /home /root -mindepth 1 -maxdepth 3 -type f | head -1 | xargs -I{} xxd -l4 "{}" 2>/dev/null',
        'd4')
    end
    # TODO tier 3 (exploit): deferred — challenge is a CTF analysis task, no remote exploit
  end
end

PcapFileTest.new.run
