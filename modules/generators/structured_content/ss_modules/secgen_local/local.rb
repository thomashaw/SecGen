#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'fileutils'

class SSModuleListGenerator < StringGenerator
  attr_accessor :filter

  LOCAL_DIR = File.expand_path('../../',__FILE__)
  TEMPLATE_PATH = "#{LOCAL_DIR}/templates/active-modules"

  def initialize
    super
    self.filter = ''
  end

  def run
    read_arguments
    self.outputs = []
    modules = File.new(TEMPLATE_PATH)
    if not self.filter.empty?
      self.filter.split(',').each { |criteria|
        IO.foreach(modules) { |line|
          # Very basic filter to capture matches based on a filter string
          if "#{line}".match(criteria)
            self.outputs << "#{line}"
          end
        }
      }
    else
      modules.each { |line| self.outputs << "#{line}"}
    end
    puts self.outputs
  end

  def read_arguments
    if ARGV.size == 0
      begin
        args_array = []
        ARGF.each do |arg|
          arg.strip.split(' ').each do |split|
            args_array << split
          end
        end
        ARGV.unshift(*args_array)
      rescue
        # Do nothing...
      end
    end

    opts = get_options

    # process option arguments
    opts.each do |opt, arg|
      if opt == '--filter'
        self.filter = arg
      end
    end
  end

  def get_options_array
    super + [['--filter', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    unless option_is_valid(opt)
      Print.err "Argument not valid: #{arg}"
      usage
      exit
    end

    case opt
      when '--filter'
        usage
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
