require_relative '../../../../../lib/post_provision_test'

class JavaDecompileTest < PostProvisionTest
  def initialize
    self.module_name = 'java_decompile'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      # A .class file is compiled and placed in a challenge directory under the user's home.
      test_local_command('java .class file placed', 'find /home /root -name "*.class" | head -1', '.class')
    end
    tier(2) do
      # Java .class files start with the magic bytes CA FE BA BE.
      test_local_command('valid Java class magic bytes', 'find /home /root -name "*.class" | head -1 | xargs -I{} xxd -l4 "{}" 2>/dev/null', 'ca fe ba be')
    end
    # TODO tier 3 (exploit): deferred — challenge is a CTF reverse-engineering task, no remote exploit
  end
end

JavaDecompileTest.new.run
