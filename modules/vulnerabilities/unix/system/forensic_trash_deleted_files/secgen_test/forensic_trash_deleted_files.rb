require_relative '../../../../../lib/post_provision_test'

class ForensicTrashDeletedFilesTest < PostProvisionTest
  def initialize
    self.module_name = 'forensic_trash_deleted_files'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # KDE/Linux Trash directory structure created under a user's home
      test_local_command('Trash dir structure exists', 'find /home -path "*/.local/share/Trash/files" -type d', 'Trash')
    end
    tier(2) do
      # Trash/files/ contains at least one deleted file
      test_local_command('deleted file in Trash', 'find /home -path "*/.local/share/Trash/files/*" -type f | head -1', '/')
      # Corresponding .trashinfo metadata file present
      test_local_command('trashinfo metadata present', 'find /home -path "*/.local/share/Trash/info/*.trashinfo" -type f | head -1', '.trashinfo')
    end
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
  end
end

ForensicTrashDeletedFilesTest.new.run
