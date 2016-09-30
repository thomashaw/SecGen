require 'spec_helper'

describe 'ssl' do
    let(:node) { 'testhost.example.de' }

    it { should contain_ssl__self_signed_certificate('testhost.example.de') }

    context 'with parameter cert_name => "otherhost.example2.de"' do
      let(:params) { { 'cert_name' => 'otherhost.example2.de' } }

      it { 
        should contain_ssl__self_signed_certificate('otherhost.example2.de').with(
          'email_address' => 'root@example2.de'
        )
      }
    end

    context 'node with long FQDN "testhost.example.co.uk"' do
      let(:node) { 'testhost.example.co.uk' }

      it {
        should contain_file("/tmp/#{node}.cnf").with_content(/^O = example$/)
      }
    end
    context 'node with three letter TLD "testhost.example.com"' do
      let(:node) { 'testhost.example.com' }

      it {
        should contain_file("/tmp/#{node}.cnf").with_content(/^C = US$/)
      }
    end
end

