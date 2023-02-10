#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'erb'
require 'fileutils'
class FilezillaConfigFileGenerator < StringGenerator
  attr_accessor :ftp_user
  attr_accessor :ftp_pass
  attr_accessor :proxy_user
  attr_accessor :proxy_pass
  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/filezilla_config.md.erb"

  def initialize
    super
    self.ftp_user = 'admin'
    self.ftp_pass = 'admin'
    self.proxy_user = ''
    self.proxy_pass = ''
  end

  def get_options_array
    super + [['--ftp_user', GetoptLong::REQUIRED_ARGUMENT],
             ['--ftp_pass', GetoptLong::REQUIRED_ARGUMENT],
             ['--proxy_user', GetoptLong::OPTIONAL_ARGUMENT],
             ['--proxy_pass', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
      when '--ftp_user'
        self.ftp_user << arg;
      when '--ftp_pass'
        self.ftp_pass << arg;
      when '--proxy_user'
        self.proxy_user << arg;
      when '--proxy_pass'
        self.proxy_pass << arg;
    end
  end
  def generate

    template_out = ERB.new(File.read(TEMPLATE_PATH), 0, '<>-')
    self.outputs << template_out.result(self.get_binding)
  end

  # Returns binding for erb files (access to variables in this classes scope)
  # @return binding
  def get_binding
    binding
  end
end


FilezillaConfigFileGenerator.new.run

