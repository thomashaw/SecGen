#!/usr/bin/ruby
require 'json'
require_relative '../../../../../../lib/objects/local_string_generator.rb'

# Generate a config hash for the XmlAlertActionConfigGenerator
class GoalMessageMap < StringGenerator
  attr_accessor :unique_id
  attr_accessor :host
  attr_accessor :sender
  attr_accessor :password
  attr_accessor :recipient
  attr_accessor :message_header
  attr_accessor :message_subtext
  attr_accessor :mapping
  attr_accessor :mapping_type

  def initialize
    super
    self.module_name = 'Goal-Message-Host AlertActioner Config Generator'
    self.unique_id = ''     # Unique ID for the module selector (or scenario level goal e.g. scenariogoal1)
    self.host = ''          # Host IP
    self.sender = ''      # Host username
    self.password = ''      # Host password
    self.recipient = ''      # Host password
    self.message_header = ''       # Message to send to host
    self.message_subtext = ''       # Message to send to host
    self.mapping = []       # TODO: Implement granular mappings
    self.mapping_type = ''
  end

  def generate
    self.outputs << { :unique_id => self.unique_id, :host => self.host, :sender => self.sender, :password => self.password, :recipient => self.recipient, :message_header => self.message_header, :message_subtext => self.message_subtext, :mapping => self.mapping, :mapping_type => self.mapping_type}.to_json
  end

  def get_options_array
    super + [['--unique_id', GetoptLong::REQUIRED_ARGUMENT],
             ['--host', GetoptLong::OPTIONAL_ARGUMENT],
             ['--sender', GetoptLong::OPTIONAL_ARGUMENT],
             ['--password', GetoptLong::OPTIONAL_ARGUMENT],
             ['--recipient', GetoptLong::OPTIONAL_ARGUMENT],
             ['--message_header', GetoptLong::OPTIONAL_ARGUMENT],
             ['--message_subtext', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--unique_id'
      self.unique_id = arg
    when '--host'
      self.host = arg
    when '--sender'
      self.sender = arg
    when '--password'
      self.password = arg
    when '--recipient'
      self.recipient = arg
    when '--message_header'
      self.message_header = arg
    when '--message_subtext'
      self.message_subtext = arg
    end
  end
end

GoalMessageMap.new.run