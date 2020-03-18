#!/usr/bin/ruby
require 'json'
require_relative '../../../../../../lib/objects/local_string_generator.rb'

# Generate a config hash for the XmlAlertActionConfigGenerator
class GoalMessageHost < StringGenerator
  attr_accessor :host
  attr_accessor :root_password
  attr_accessor :message
  attr_accessor :mapping
  attr_accessor :mapping_type

  def initialize
    super
    self.module_name = 'Goal-Message-Host AlertActioner Config Generator'
    self.host = ''          # Host IP (TODO: Consider expanding this to an array of multiple targets)
    self.root_password = '' # Host root password
    self.message = ''       # Message to send to host
    self.mapping = []       # TODO: Implement granular mappings
    self.mapping_type = ''
  end

  def generate
    # TODO: Create an enum-like hash/class to validate the mapping_types
    self.outputs << {:message => self.message, :root_password => self.root_password, :host => self.host, :mapping => self.mapping, :mapping_type => self.mapping_type}.to_json
  end

  def get_options_array
    super + [['--message', GetoptLong::REQUIRED_ARGUMENT],
             ['--host', GetoptLong::REQUIRED_ARGUMENT],
             ['--root_password', GetoptLong::REQUIRED_ARGUMENT],
             ['--mapping', GetoptLong::OPTIONAL_ARGUMENT],
             ['--mapping_type', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--message'
      self.message << arg
    when '--root_password'
      self.root_password << arg
    when '--host'
      self.host << arg
    when '--mapping'
      self.mapping << arg
    when '--mapping_type'
      self.mapping_type << arg
    end
  end
end

GoalMessageHost.new.run