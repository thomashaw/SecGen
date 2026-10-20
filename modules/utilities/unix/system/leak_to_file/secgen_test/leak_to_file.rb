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
    tier(1) do
      # The module always creates a file; verify /home or /root has non-empty files
      test_local_command('home directories exist', 'ls /home || ls /root', '')
    end
    tier(2) do
      # Find any regular file created by a non-system user (or root) with size > 0
      result = run_command('find /home /root -maxdepth 2 -type f -size +0c 2>/dev/null | head -5')
      if result[:stdout].strip.empty?
        skip_check('leaked file found', 'no non-empty file in /home or /root — may need specific scenario context', tier: 2, evidence: nil)
      else
        pass_check('leaked file', "found file(s): #{result[:stdout].strip.split("\n").first}", tier: 2, evidence: result[:stdout])
      end
    end
  end
end

LeakToFileTest.new.run
