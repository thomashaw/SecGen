require 'spec_helper'

describe 'ssl::self_signed_certificate' do
  let(:title) { 'testhost.example.de' }
  let(:node)  { 'testhost.example.de' }

  directory = "/etc/ssl"

  context "with default parameters" do


    it { should contain_class('ssl::install') }

    it { should contain_file(directory).with_ensure('directory') }

    it {
      should contain_file("#{directory}/#{node}.cnf").with_content(/^CN = #{node}$/)
    }

  end

  context "parameter country => 'COM'" do
    let(:params) { { 'country' => 'COM' } }
    it { should raise_error(Puppet::Error) }
  end

  context "parameter country => 'DE', organization => 'example'" do
    let(:params) { 
      {
        'country'      => 'DE',
        'organization' => 'example'
      } 
    } 
    it {
      should contain_file("#{directory}/#{node}.cnf").with_content(/^C = DE$/)
    }
    it {
      should contain_file("#{directory}/#{node}.cnf").with_content(/^O = example$/)
    }
  end

end
