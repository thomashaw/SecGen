require 'beaker-rspec'
require 'pry'

hosts.each do |host|
  install_puppet
end

UNSUPPORTED_PLATFORMS = ['Suse','windows','AIX','Solaris']

RSpec.configure do |c|
  # Project root
  proj_root = File.expand_path(File.join(File.dirname(__FILE__), '..'))

  # Readable test descriptions
  c.formatter = :documentation

  # Configure all nodes in nodeset
  c.before :suite do
    # Install module
    puppet_module_install(:source => proj_root, :module_name => 'ssl')
    hosts.each do |host|
      on host, puppet('module','install','puppetlabs-stdlib'), { :acceptable_exit_codes => [0,1] }
    end
  end
end
