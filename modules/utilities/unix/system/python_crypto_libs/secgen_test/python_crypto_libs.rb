require_relative '../../../../../lib/post_provision_test'

class PythonCryptoLibsTest < PostProvisionTest
  def initialize
    self.module_name = 'python_crypto_libs'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    tier(1) do
      test_local_command('python3-pip installed', 'dpkg -l python3-pip | grep -E "^ii"', 'python3-pip')
      test_local_command('python3-numpy installed', 'dpkg -l python3-numpy | grep -E "^ii"', 'python3-numpy')
      test_local_command('python3-pycryptodome installed',
                         'dpkg -l python3-pycryptodome | grep -E "^ii"',
                         'python3-pycryptodome')
    end
    tier(2) do
      test_command_succeeds('numpy importable', 'python3 -c "import numpy; print(numpy.__version__)"')
      test_command_succeeds('Crypto importable', 'python3 -c "from Crypto.Cipher import AES; print(\'ok\')"')
    end
  end
end

PythonCryptoLibsTest.new.run
