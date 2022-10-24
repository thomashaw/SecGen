#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class SymmetricEncAES < HackerbotConfigGenerator

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Symmetric/AES'
    self.title = 'Symmetric Encryption with Advanced Encryption Standard (AES)'

    self.local_dir = File.expand_path('../../',__FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/symmetric_enc_aes_lab.xml.erb"
    self.html_template_path = "#{self.local_dir}/templates/labsheet.html.erb"
  end

end

SymmetricEncAES.new.run