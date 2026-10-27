require_relative '../../../../../lib/post_provision_test'

class PythonTest < PostProvisionTest
  def initialize
    self.module_name = 'python'
    self.module_path = get_module_path(__FILE__)
    super
  end

  # No read_facts or default inputs: nothing to parameterise. python.pp installs
  # the system python with pip present; dev, virtualenv and gunicorn are absent.
  def test_module
    super
    tier(1) do
      test_local_command('python2 on PATH?', 'which python2 || which python', 'python')
      test_local_command('python package installed?', "dpkg -l | grep -E '^ii +python2?( |:)'", 'python')
      test_local_command('pip on PATH?', 'which pip || which pip2', 'pip')
    end
    tier(2) do
      test_local_command('python2 version', 'p=$(command -v python2 || command -v python); $p --version 2>&1', 'Python 2')
      test_local_command('python runs code as an unprivileged user',
                         'p=$(command -v python2 || command -v python); $p -c "print(6*7)"', '42', user: 'nobody')
      test_local_command('python imports a standard library module',
                         'p=$(command -v python2 || command -v python); $p -c "import json; print(json.dumps([1]))"', '[1]', user: 'nobody')
      test_local_command('pip runs for python 2', '(pip2 --version || pip --version) 2>&1', 'python 2', user: 'nobody')
    end
  end
end

PythonTest.new.run
