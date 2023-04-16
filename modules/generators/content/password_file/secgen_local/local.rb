#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'erb'
require 'fileutils'
class PasswordFileGenerator < StringGenerator
  attr_accessor :website_sample
  attr_accessor :username_sample
  attr_accessor :pass_list
  attr_accessor :password_1
  attr_accessor :password_2
  attr_accessor :password_3
  attr_accessor :password_4
  attr_accessor :password_5
  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/password_file.md.erb"

  def initialize
    super
    self.password_1 = ''
    self.password_2 = ''
    self.password_3 = ''
    self.password_4 = ''
    self.password_5 = ''
  end
  
  def get_options_array
    super + [['--password1', GetoptLong::OPTIONAL_ARGUMENT],
             ['--password2', GetoptLong::OPTIONAL_ARGUMENT],
             ['--password3', GetoptLong::OPTIONAL_ARGUMENT],
             ['--password4', GetoptLong::OPTIONAL_ARGUMENT],
             ['--password5', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--password1'
      self.password_1 << arg;
    when '--password2'
      self.password_2 << arg;
    when '--password3'
      self.password_3 << arg;
    when '--password4'
      self.password_4 << arg;
    when '--password5'
      self.password_5 << arg;
    end
  end

def generate
  pass_list = Array.new
  passCount = 5

  if self.password_1 != ''
    passCount -= 1
    pass_list.append(self.password_1)
  end

  if self.password_2 != ''
    passCount -= 1
    pass_list.append(self.password_2)
  end

  if self.password_3 != ''
    passCount -= 1
    pass_list.append(self.password_3)
  end

  if self.password_4 != ''
    passCount -= 1
    pass_list.append(self.password_4)
  end

  if self.password_5 != ''
    passCount -= 1
    pass_list.append(self.password_5)
  end

  pass_array = File.readlines('../../../../../lib/resources/wordlists/10_million_password_list_top_100')
  website_array = File.readlines('../../../../../lib/resources/linelists/top_100_websites')
  self.website_sample = website_array.sample(10)
  username_array = File.readlines('../../../../../lib/resources/wordlists/mythical_creatures')
  self.username_sample = username_array.sample(5)

  if passCount == 0
    self.pass_sample = pass_array.sample(5)
  elsif passCount < 5
    self.pass_sample = pass_array.sample(5-passCount)
    self.pass_list.append(self.pass_sample)
  end

  pass_list.each { |pass| pass }

  template_out = ERB.new(File.read(TEMPLATE_PATH), 0, '<>-')
  self.outputs << template_out.result(self.get_binding)
end


  # Returns binding for erb files (access to variables in this classes scope)
  # @return binding
  def get_binding
    binding
  end
end

PasswordFileGenerator.new.run

