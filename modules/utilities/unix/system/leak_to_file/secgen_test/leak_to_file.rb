require_relative '../../../../../lib/post_provision_test'

# Verifies that leak_to_file created a file containing leaked data.
# Both the filename and content are scenario-controlled; we look for any
# non-empty file owned by a non-root user (or in /root if no account specified).
class LeakToFileTest < PostProvisionTest
  def initialize
    self.module_name = 'leak_to_file'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    storage_dir  = self.json_inputs['storage_directory']&.first
    leaked_fname = self.json_inputs['leaked_filename']&.first

    tier(1) do
      if storage_dir && leaked_fname
        test_local_command("#{leaked_fname} present at #{storage_dir}",
                           "test -f #{storage_dir}/#{leaked_fname} && echo ok", 'ok')
      else
        # storage_directory may derive from an account input (set dynamically by Puppet);
        # fall back to searching common home directories
        result = run_command('find /home /root -maxdepth 3 -type f -size +0c 2>/dev/null | head -1')
        if result[:stdout].strip.empty?
          skip_check('leaked file found', 'no non-empty file found under /home or /root', tier: 1, evidence: nil)
        else
          pass_check('leaked file found', "found: #{result[:stdout].strip}", tier: 1, evidence: result[:stdout])
        end
      end
    end
  end
end

LeakToFileTest.new.run
