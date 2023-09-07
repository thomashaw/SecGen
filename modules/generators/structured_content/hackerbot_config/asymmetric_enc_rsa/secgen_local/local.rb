#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class AsymmetricEncRSA < HackerbotConfigGenerator

  attr_accessor :rsa_challenges
  attr_accessor :desktop_ip
  attr_accessor :hackerbot_server_ip

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Asymmetric/RSA'
    self.title = 'Public-Key Cryptography with RSA'

    self.local_dir = File.expand_path('../../', __FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/asymmetric_enc_rsa_lab.xml.erb"
    self.html_template_path = "#{self.local_dir}/templates/labsheet.html.erb"
    self.rsa_challenges = []
    self.desktop_ip = ''
    self.hackerbot_server_ip = ''
  end

  def get_options_array
    super + [['--rsa_challenges', GetoptLong::REQUIRED_ARGUMENT],
             ['--desktop_ip', GetoptLong::REQUIRED_ARGUMENT],
             ['--hackerbot_server_ip', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
      when '--rsa_challenges'
        self.rsa_challenges << arg;
      when '--desktop_ip'
        self.desktop_ip << arg;
      when '--hackerbot_server_ip'
        self.hackerbot_server_ip << arg;
    end
  end
end

AsymmetricEncRSA.new.run