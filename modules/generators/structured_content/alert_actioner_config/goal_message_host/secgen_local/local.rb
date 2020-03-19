#!/usr/bin/ruby
require 'json'
require_relative '../../../../../../lib/objects/local_string_generator.rb'

# Generate a config hash for the XmlAlertActionConfigGenerator
class GoalMessageHost < StringGenerator
  attr_accessor :host
  attr_accessor :username
  attr_accessor :password
  attr_accessor :message_header
  attr_accessor :message_subtext
  attr_accessor :mapping
  attr_accessor :mapping_type

  def initialize
    super
    self.module_name = 'Goal-Message-Host AlertActioner Config Generator'
    self.host = ''          # Host IP (TODO: Consider expanding this to an array of multiple targets)
    self.username = ''      # Host username
    self.password = ''      # Host password
    self.message_header = ''       # Message to send to host
    self.message_subtext = ''       # Message to send to host
    self.mapping = []       # TODO: Implement granular mappings
    self.mapping_type = ''
  end

  def generate
    # TODO: Create an enum-like hash/class to validate the mapping_types
    self.outputs << {:host => self.host, :username => self.username, :password => self.password, :message_header => self.message_header, :message_subtext => self.message_subtext, :mapping => self.mapping, :mapping_type => self.mapping_type}.to_json
  end

  def get_options_array
    super + [['--host', GetoptLong::REQUIRED_ARGUMENT],
             ['--username', GetoptLong::REQUIRED_ARGUMENT],
             ['--password', GetoptLong::REQUIRED_ARGUMENT],
             ['--message_header', GetoptLong::REQUIRED_ARGUMENT],
             ['--message_subtext', GetoptLong::OPTIONAL_ARGUMENT],
             ['--mapping', GetoptLong::OPTIONAL_ARGUMENT],
             ['--mapping_type', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--host'
      self.host << arg
    when '--username'
      self.username << arg
    when '--password'
      self.password << arg
    when '--message_header'
      self.message_header << arg
    when '--message_subtext'
      self.message_subtext << arg
    when '--mapping'
      self.mapping << arg
    when '--mapping_type'
      self.mapping_type << arg
    end
  end
end

GoalMessageHost.new.run