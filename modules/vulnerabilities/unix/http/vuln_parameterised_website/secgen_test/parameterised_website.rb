require_relative '../../../../../lib/post_provision_test'
require 'json'

class ParamWebsiteTest < PostProvisionTest
  attr_accessor :organisation

  def initialize
    self.module_name = 'vuln_parameterised_website'
    self.module_path = get_module_path(__FILE__)
    super
  end

  def test_module
    super
    json_inputs = get_json_inputs
    test_service_up
    test_html_returned_content("/css/#{json_inputs['theme'][0]}", 'Bootswatch v4.0.0')
    test_org_functionality(json_inputs)
    test_additional_page(json_inputs)
    test_security_audit_remit(json_inputs)
    test_acceptable_use_policy(json_inputs)
  end

  def get_organisation(json_inputs)
    JSON.parse(json_inputs['organisation'][0])
  end

  def test_org_functionality(json_inputs)
    if json_inputs['organisation'] and
        json_inputs['organisation'][0] and
        json_inputs['organisation'][0] != ''

      organisation = get_organisation(json_inputs)
      employee_1 = organisation['employees'][0]

      test_html_returned_content('/index.html', organisation['business_name'])
      test_html_returned_content('/contact.html', organisation['business_motto'])
      test_html_returned_content('/contact.html', employee_1['name'])
    end
  end

  def test_security_audit_remit(json_inputs)
    if json_inputs['security_audit'] and
        json_inputs['security_audit'][0] and
        json_inputs['security_audit'][0] != ''
      test_html_returned_content('/security_audit_remit.html', "Security Audit Remit of #{get_organisation(json_inputs)['business_name']}")
    end
  end

  def test_acceptable_use_policy(json_inputs)
    if json_inputs['host_acceptable_use_policy'] and
        json_inputs['host_acceptable_use_policy'][0] and
        json_inputs['host_acceptable_use_policy'][0] == 'true'
      test_html_returned_content('/acceptable_use_policy.html', "Acceptable Use Policy")
      test_html_returned_content('/acceptable_use_policy.html', get_organisation(json_inputs)['business_name'])
    end
  end

  def test_additional_page(json_inputs)
    if json_inputs['additional_page_filenames'] and
        json_inputs['additional_page_filenames'][0] and
        json_inputs['additional_page_filenames'][0].include? 'html' and
        json_inputs['additional_pages'] and
        json_inputs['additional_pages'][0]

      page_name = json_inputs['additional_page_filenames'][0]
      page_name = "/#{page_name}" if page_name.split[0] != '/'

      test_html_returned_content(page_name, json_inputs['additional_pages'][0], true)
    end
  end

end

ParamWebsiteTest.new.run