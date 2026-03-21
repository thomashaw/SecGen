#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class NetworksIntro < HackerbotConfigGenerator

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Networks Intro'
    self.title = 'Network Security Fundamentals: Network Design, Segmentation, NAT & DMZ'

    self.local_dir = File.expand_path('../../',__FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/lab.xml.erb"
    self.html_template_path = "#{self.local_dir}/templates/labsheet.html.erb"
  end

end

NetworksIntro.new.run