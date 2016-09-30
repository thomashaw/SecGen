require 'spec_helper'


describe 'ssl::install' do
    it { should contain_package('openssl') }
end
