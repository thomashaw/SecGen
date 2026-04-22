#!/usr/bin/ruby
require 'json'
require_relative '../../../../../../lib/objects/local_string_generator.rb'

class GoalFlagHacktivity < StringGenerator
  attr_accessor :hacktivity_url
  attr_accessor :mapping
  attr_accessor :mapping_type

  def initialize
    super
    self.module_name = 'Goal-Flag to Hacktivity AlertActioner Config Generator'
    self.hacktivity_url = ''    # Address for Hacktivity / external web application
    self.mapping = []   # TODO: Implement granular mappings
    self.mapping_type = ''
  end

  def generate
    # TODO: Create an enum-like hash/class to validate the mapping_types
    self.outputs << {:hacktivity_url => self.hacktivity_url, :mapping => self.mapping, :mapping_type => self.mapping_type}.to_json
  end

  def get_options_array
    super + [['--hacktivity_url', GetoptLong::REQUIRED_ARGUMENT],
             ['--mapping', GetoptLong::OPTIONAL_ARGUMENT],
             ['--mapping_type', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--hacktivity_url'
      self.hacktivity_url = arg
    when '--mapping'
      self.mapping << arg
    when '--mapping_type'
      self.mapping_type << arg
    end
  end
end

GoalFlagHacktivity.new.run