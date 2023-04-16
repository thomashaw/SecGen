#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'erb'
require 'fileutils'
class BashHistoryGenerator < StringGenerator
  attr_accessor :command_sample
  attr_accessor :sudo_sample
  attr_accessor :password_sample
  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/bash_history.md.erb"

  def initialize
    super
    self.password_sample = ''
  end

  def get_options_array
    super + [['--password', GetoptLong::OPTIONAL_ARGUMENT]]
  end
  
  def proccess_options(opt, arg)
    super
    case opt
    when '--password'
      self.password_sample << arg;
    end
  end

  def generate
    sudo_array = File.readlines('../../../../../lib/resources/linelists/top_50_sudo_commands')
    command_array = File.readlines('../../../../../lib/resources/linelists/top_90_linux_commands')
    puts "Password = #{self.password_sample}"
    if self.password_sample != ''
    self.sudo_sample = sudo_array.sample(5)
    self.command_sample = command_array.sample(20)
    counter = 4
    sudo_count = 0
    while counter != 20 
      randInt = rand(sudo_sample.length)
      command_sample.insert(randInt, sudo_sample[randInt])
      if sudo_count == 0
        command_sample.insert(5, self.password_sample)
        sudo_count += 1
      end
      counter += 4
    end
    else
      self.command_sample = command_array.sample(30)
    end
    template_out = ERB.new(File.read(TEMPLATE_PATH), 0, '<>-')
    self.outputs << template_out.result(self.get_binding)
  end

  # Returns binding for erb files (access to variables in this classes scope)
  # @return binding
  def get_binding
    binding
  end
end

BashHistoryGenerator.new.run

