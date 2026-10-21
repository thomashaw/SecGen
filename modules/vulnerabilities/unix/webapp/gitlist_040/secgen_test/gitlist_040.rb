require_relative '../../../../../lib/post_provision_test'

class Gitlist040Test < PostProvisionTest
  def initialize
    self.module_name = 'gitlist_040'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    test_html_returned_content('/', '<title>GitList</title>')
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/multi/http/gitlist_arg_injection
  end
end

Gitlist040Test.new.run