#!/usr/bin/ruby
require 'json'
require_relative '../../../../../lib/objects/local_string_encoder.rb'

class FilePathGenerator < StringEncoder
  attr_accessor :file_name
  attr_accessor :storage_directory

  def initialize
    super
    self.module_name = 'File Path Generator'
    self.file_name = ''
    self.storage_directory = ''
  end

  def encode_all
    file_name = self.file_name
    storage_directory = self.storage_directory

    if storage_directory[-1] != '/'
      storage_directory = storage_directory + '/'
    end

    self.outputs << storage_directory + file_name
  end

  def process_options(opt, arg)
    super
    case opt
      when '--file_name'
        self.extension << arg;
      when '--storage_directory'
        self.storage_directory << arg;
    end
  end

  def get_options_array
    super + [['--file_name', GetoptLong::OPTIONAL_ARGUMENT],
             ['--storage_directory', GetoptLong::OPTIONAL_ARGUMENT]]
  end

end

FilePathGenerator.new.run
