#!/usr/bin/ruby
require 'json'
require_relative '../../../../../lib/objects/local_string_generator.rb'

class AAAConfigGenerator < StringGenerator
  attr_accessor :server_ip
  attr_accessor :client_ips
  attr_accessor :elasticsearch_port
  attr_accessor :logstash_port
  attr_accessor :kibana_port
  attr_accessor :aa_configs

  def initialize
    super
    self.module_name = 'Analysis Alert Action Config Generator'
    self.client_ips = []
    self.aa_configs = []
  end

  def generate

    # Validate the inputs + crash out if we don't receive all inputs.
    self.outputs << {
        :server_ip => self.server_ip,
        :client_ips => self.client_ips,
        :elasticsearch_port => self.elasticsearch_port,
        :logstash_port => self.logstash_port,
        :kibana_port => self.kibana_port,
        :aa_configs => self.aa_configs
    }.to_json
  end

  def get_options_array
    super + [['--server_ip', GetoptLong::REQUIRED_ARGUMENT],
             ['--client_ips', GetoptLong::REQUIRED_ARGUMENT],
             ['--elasticsearch_port', GetoptLong::REQUIRED_ARGUMENT],
             ['--logstash_port', GetoptLong::REQUIRED_ARGUMENT],
             ['--kibana_port', GetoptLong::REQUIRED_ARGUMENT],
             ['--aa_configs', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--server_ip'
      self.server_ip = arg
    when '--client_ips'
      self.client_ips << arg
    when '--elasticsearch_port'
      self.elasticsearch_port = arg
    when '--logstash_port'
      self.logstash_port = arg
    when '--kibana_port'
      self.kibana_port = arg
    when '--aa_configs'
      self.aa_configs << JSON.parse(arg)
    end
  end
end

AAAConfigGenerator.new.run