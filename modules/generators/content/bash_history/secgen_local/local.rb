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

  def generate

    sudo_array = File.readlines('../../../../../lib/resources/linelists/top_50_sudo_commands')
    self.sudo_sample = sudo_array.sample(5)
    password_array = File.readlines('../../../../../lib/resources/wordlists/10_million_password_list_top_100')
    self.password_sample = password_array.sample(1)
    command_array = File.readlines('../../../../../lib/resources/linelists/top_90_linux_commands')
    self.command_sample = command_array.sample(20)
    command_array.insert(4, sudo_array)
    counter = 4
    sudo_count = 0
    while counter != 20
      command_sample.insert(counter, sudo_sample[sudo_count])
      if sudo_count == 0
        command_sample.insert(5, password_sample[0])
        sudo_count += 1
      end
      counter += 4
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

