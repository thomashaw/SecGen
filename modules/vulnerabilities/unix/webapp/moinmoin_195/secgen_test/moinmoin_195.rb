require_relative '../../../../../lib/post_provision_test'

class MoinMoin195Test < PostProvisionTest
  def initialize
    self.module_name = 'moinmoin_195'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    test_html_returned_content('/index.html','<script type="text/javascript" src="/moin_static195/common/js/common.js"></script>')
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/unix/webapp/moinmoin_twikidraw
  end
end

MoinMoin195Test.new.run