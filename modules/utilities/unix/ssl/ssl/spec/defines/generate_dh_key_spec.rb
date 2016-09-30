require 'spec_helper'

describe 'ssl::generate_dh_key' do
  let(:title) { 'dh_512' }

  it { is_expected.to contain_class('ssl::install') }

  it { is_expected.to contain_file('/etc/ssl') }
  it { is_expected.to contain_exec('create DH key dh_512.pem')\
       .that_comes_before('File[/etc/ssl/dh_512.pem]')\
       .with_command(/-2 512$/)\
       .with_creates('/etc/ssl/dh_512.pem')
  }
  it do
    is_expected.to contain_file('/etc/ssl/dh_512.pem').with(
    { 'mode'  => '0644',
      'owner' => 'root',
      'group' => 'root'
    } )
  end

  describe "parameter directory" do
    context "=> '/other'" do
      let(:params) { { 'directory' => '/other' } }

      it { is_expected.to contain_file('/other') }
      it { is_expected.to contain_exec('create DH key dh_512.pem')\
           .that_comes_before('File[/other/dh_512.pem]')\
           .with_creates('/other/dh_512.pem') }
      it { is_expected.to contain_file('/other/dh_512.pem') }
    end
    context "=> 'breakme'" do
      let(:params) { { 'directory' => 'breakme' } }
      it { is_expected.to raise_error(/"breakme" is not an absolute path/) }
    end
  end

  describe "parameter owner" do
    context "=> 'other'" do
      let(:params) { { 'owner' => 'other' } }
      it { is_expected.to contain_file('/etc/ssl/dh_512.pem').with_owner('other') }
    end
    context "=> { :test => 1 } (hash)" do
      let(:params) { { 'owner' => { 'test' => 1 } } }
      it { is_expected.to raise_error(/is not a string/) }
    end
  end

  describe "parameter group" do
    context "=> 'other'" do
      let(:params) { { 'group' => 'other' } }
      it { is_expected.to contain_file('/etc/ssl/dh_512.pem').with_group('other') }
    end
    context "=> { :test => 1 } (hash)" do
      let(:params) { { 'group' => { 'test' => 1 } } }
      it { is_expected.to raise_error(/is not a string/) }
    end
  end

  describe "parameter numbits" do
    context "=> 1024 (integer)" do
      let(:params) { { 'numbits' => 1024 } }

      it { is_expected.to contain_exec('create DH key dh_512.pem')\
           .with_command(/1024$/) }
    end
    context "=> 'breakme'" do
      let(:params) { { 'numbits' => 'breakme' } }
      it { is_expected.to raise_error(/"breakme" does not match/) }
    end
  end

  describe "parameter generator" do
    context "=> 5 (integer)" do
      let(:params) { { 'generator' => 5 } }

      it { is_expected.to contain_exec('create DH key dh_512.pem')\
           .with_command(/ -5 /) }
    end
    context "=> 'breakme'" do
      let(:params) { { 'generator' => 'breakme' } }
      it { is_expected.to raise_error(/"breakme" does not match/) }
    end
  end



end
