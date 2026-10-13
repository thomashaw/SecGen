# Regression tests for combined_path.rb, covering every path form used by the
# correctness cases (and the decoy forms found in pre-run validation).
#
#   ruby test_combined_path.rb                  # tests the deployed module copy
#   ruby test_combined_path.rb before           # shows how the original fails
require 'minitest/autorun'

VERSION = ARGV.delete('before') ? 'before' : 'after'
load(VERSION == 'before' ? File.join(__dir__, 'before', 'combined_path.rb')
                         : File.expand_path('../../../modules/services/unix/logging/logstash/files/combined_path.rb', __dir__))

# Minimal stand-in for a Logstash event.
class FakeEvent
  def initialize(fields) @f = fields end
  def get(k) @f[k] end
  def set(k, v) @f[k] = v end
end

W = '/home/challenger/.sscorrect'.freeze

class TestCombinedPath < Minitest::Test
  # [case, cwd, file.path, expected combined_path]
  CASES = [
    ['P01 absolute',               '/home/challenger',      '/etc/shadow',             '/etc/shadow'],
    ['P01 from cwd /',             '/',                     '/etc/shadow',             '/etc/shadow'],
    ['P02 relative',               '/etc',                  'shadow',                  '/etc/shadow'],
    ['P03 parent traversal',       '/home/challenger',      '../../etc/shadow',        '/etc/shadow'],
    ['P04 dot segment',            '/home/challenger',      '/etc/./shadow',           '/etc/shadow'],
    ['P05 repeated slashes',       '/home/challenger',      '//etc//shadow',           '/etc/shadow'],
    ['P06 traversal in absolute',  '/home/challenger',      '/etc/../etc/shadow',      '/etc/shadow'],
    ['P08 cwd via symlink',        '/etc',                  'shadow',                  '/etc/shadow'],
    ['.. above root',              '/',                     '../../etc/shadow',        '/etc/shadow'],
    ['N01 decoy absolute',         '/home/challenger',      "#{W}/decoy/etc/shadow",   "#{W}/decoy/etc/shadow"],
    ['N02 decoy relative',         "#{W}/decoy/etc",        'shadow',                  "#{W}/decoy/etc/shadow"],
    ['N03 decoy relative dir',     "#{W}/decoy",            'etc/shadow',              "#{W}/decoy/etc/shadow"],
    ['N11 decoy .//',              "#{W}/decoy",            './/etc/shadow',           "#{W}/decoy/etc/shadow"],
    ['N12 decoy x//',              "#{W}/decoy",            'x//etc/shadow',           "#{W}/decoy/x/etc/shadow"],
    # The kernel follows the symlink L (four levels below decoy/) before applying
    # '..', so it opens decoy/etc/shadow; lexical normalisation cannot follow L
    # and yields /etc/shadow. This remains a false detection by design (the
    # analysis server cannot resolve links on the monitored host), as P07 remains
    # a miss.
    ['N13 decoy symlink then ..',  W,                       'L/../../../../etc/shadow', '/etc/shadow'],
    ['N09 openat relative name',   '/home/challenger',      'shadow',                  '/home/challenger/shadow'],
  ].freeze

  CASES.each_with_index do |(name, cwd, path, expected), i|
    define_method("test_#{i}_#{name.gsub(/\W+/, '_')}") do
      e = FakeEvent.new('[process][working_directory]' => cwd, '[file][path]' => path)
      filter(e)
      assert_equal expected, e.get('combined_path'), "#{name}: cwd=#{cwd} path=#{path}"
      refute_includes e.get('combined_path'), '//' if VERSION == 'after'
    end
  end

  def test_absolute_path_without_cwd
    e = FakeEvent.new('[file][path]' => '/etc/shadow')
    filter(e)
    expected = VERSION == 'after' ? '/etc/shadow' : nil
    expected ? assert_equal(expected, e.get('combined_path')) : assert_nil(e.get('combined_path'))
  end
end
