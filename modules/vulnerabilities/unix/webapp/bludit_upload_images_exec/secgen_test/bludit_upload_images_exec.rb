require_relative '../../../../../lib/post_provision_test'

class BluditTest < PostProvisionTest
  def initialize
    self.module_name = 'bludit_upload_images_exec'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    test_service_up
    test_html_returned_content('/', '<title>Bludit</title>')
    # TODO tier 3 (exploit): deferred — wire up test_msf_exploit when attacker VM is available
    # msf_module: exploit/linux/http/bludit_upload_images_exec
  end
end

BluditTest.new.run
