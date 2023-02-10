#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'erb'
require 'fileutils'
class PasswordFileGenerator < StringGenerator
  attr_accessor :website_sample
  attr_accessor :username_sample
  attr_accessor :pass_sample
  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/password_file.md.erb"

  def generate

    website_array = File.readlines('../../../../../lib/resources/linelists/top-100-websites.txt')
    self.website_sample = website_array.sample(10)
    username_array = File.readlines('../../../../../lib/resources/wordlists/mythical_creatures')
    self.username_sample = username_array.sample(5)
    pass_array = File.readlines('../../../../../lib/resources/wordlists/10_million_password_list_top_100')
    self.pass_sample = pass_array.sample(10)
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

