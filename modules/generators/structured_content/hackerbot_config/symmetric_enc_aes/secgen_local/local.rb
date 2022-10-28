#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class SymmetricEncAES < HackerbotConfigGenerator

  attr_accessor :aes_challenges

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Symmetric/AES'
    self.title = 'Symmetric Encryption with Advanced Encryption Standard (AES)'

    self.local_dir = File.expand_path('../../', __FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/symmetric_enc_aes_lab.xml.erb"
    self.html_template_path = "#{self.local_dir}/templates/labsheet.html.erb"
    self.aes_challenges = []
  end

  def get_options_array
    super + [['--aes_challenges', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--aes_challenges'
      Print.info("AES Challenges Added to HB Config: " + arg)
      self.aes_challenges << arg;
    end
  end
end

SymmetricEncAES.new.run