require_relative '../../../../../lib/post_provision_test'

class MazeTest < PostProvisionTest
  def initialize
    self.module_name = 'maze'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('ruby installed', 'which ruby', 'ruby')
      # maze.rb is dropped in a challenge dir with restricted permissions (setgid).
      test_local_command('maze.rb placed', 'find /home /root -name "maze.rb" | head -1', 'maze.rb')
    end
    tier(2) do
      # The maze.rb script should be readable by group (mode 0440).
      test_local_command('maze.rb readable', 'find /home /root -name "maze.rb" -perm /0440 | head -1', 'maze.rb')
    end
    # TODO tier 3 (exploit): deferred — challenge is a CTF programming task, no remote exploit
  end
end

MazeTest.new.run
