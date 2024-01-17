#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'fileutils'
require 'json'

class SSModuleListGenerator < StringGenerator
  attr_accessor :filter

  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/active-modules"

  def initialize
    super

    self.module_name = 'Security Shepherd Module Generator'

    self.filter = ''
  end

  def generate
    modules_file = File.new(TEMPLATE_PATH)
    if not self.filter.empty?
      # Returns string based around the format (search_term)(\s\d)+
      self.filter.split(/,/).each { |filter_term|
        words = filter_term.match(/[^\d|\n]+/).to_s.strip;
        filter_nos = filter_term.split(/\s+/).select!{|item| item.to_s.match?(/\d/)};

        modules_file = File.new(TEMPLATE_PATH)
        modules_file.each { |line|
          if line.include?(words) && filter_nos.size == 0
            self.outputs << "#{line}".strip
          elsif filter_nos.size > 0
            filter_nos.each { |number|
              if line.include?(words) && line.include?(number)
                self.outputs << "#{line}".strip
              end
            }
          end
        }
        modules_file.close;
      }
    else
      modules_file.each { |line|
        self.outputs << "#{line}".strip;
      }
      modules_file.close;
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
