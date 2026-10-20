require_relative '../../../../../lib/post_provision_test'

class ZipFileTest < PostProvisionTest
  def initialize
    self.module_name = 'zip_file'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('leaked file exists', 'find /home /root -mindepth 1 -maxdepth 3 -type f | head -1', '/')
    end
    tier(2) do
      # ZIP files start with PK magic bytes (50 4b 03 04).
      test_local_command('zip magic bytes present',
        'find /home /root -mindepth 1 -maxdepth 3 -type f | head -1 | xargs -I{} xxd -l4 "{}" 2>/dev/null',
        '50 4b')
    end
    # TODO tier 3 (exploit): deferred — challenge is a CTF analysis task, no remote exploit
  end
end

ZipFileTest.new.run
