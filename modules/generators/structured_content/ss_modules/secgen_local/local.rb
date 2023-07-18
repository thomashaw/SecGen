#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'fileutils'

class SSModuleListGenerator < StringGenerator
  attr_accessor :filter

  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/active-modules"

  def initialize
    super

    self.module_name = 'Security Shepherd Module Generator'
    self.filter = []
  end

  def generate
    modules = File.new(TEMPLATE_PATH)
    if not self.filter.empty?
      self.filter.each { |criteria|
        IO.foreach(modules) { |line|
          # Very basic filter to capture matches based on a filter string
          if "#{line}".match(criteria)
            self.outputs << "#{line}".strip
          end
        }
      }
    else
      modules.each { |line| self.outputs << "#{line}".strip}
    end
  end

  def get_options_array
    super + [['--filter', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super

    case opt
      when '--filter'
        self.filter << arg
    end
  end

  def usage
    Print.err "Usage:
   #{$0} [--options]

   OPTIONS:
     --filter [String]
"
    exit
  end
end

SSModuleListGenerator.new.run
