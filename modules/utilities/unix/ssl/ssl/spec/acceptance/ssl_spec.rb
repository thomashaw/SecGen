require 'spec_helper_acceptance'

describe 'ssl class', :unless => UNSUPPORTED_PLATFORMS.include?(fact('osfamily')) do

  fqdn   = 'testhost.example.de'
  domain = 'example.de'

  context 'default parameteres' do
    it 'should work with no errors' do
      pp = <<-EOS
      class{'ssl':
        cert_name => 'testhost.example.de',
      }
      EOS

      apply_manifest(pp, :catch_failures => true)
      expect(apply_manifest(pp, :catch_failures => true).exit_code).to be_zero
   end

   describe package('openssl') do
     it { should be_installed }
   end

   describe file("/tmp/#{fqdn}.crt") do
     it { should be_file }
   end

   describe command("openssl x509 -in /tmp/#{fqdn}.crt -noout -subject") do
     it { should return_stdout /^subject= \/CN=#{fqdn}\/emailAddress=root@#{domain}\/C=.*$/ }
   end

 end

end
