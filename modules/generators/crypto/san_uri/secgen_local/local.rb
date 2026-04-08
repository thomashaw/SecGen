#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_encoder.rb'
require 'securerandom'

class SanUriGenerator < StringEncoder

  attr_accessor :domain, :protocol, :port

  def initialize
    super
    self.module_name = 'SAN URI Generator'
    self.domain   = 'localhost'
    self.protocol = 'http'
    self.port     = ''
  end

  def encode_all
    hex_path = SecureRandom.hex(16)

    if self.port && !self.port.empty?
      uri = "#{self.protocol}://#{self.domain}:#{self.port}/#{hex_path}/"
    else
      uri = "#{self.protocol}://#{self.domain}/#{hex_path}/"
    end

    self.outputs << uri
  end

  def process_options(opt, arg)
    super
    case opt
    when '--domain'
      self.domain = arg
    when '--protocol'
      self.protocol = arg
    when '--port'
      self.port = arg
    end
  end

  def get_options_array
    super + [
      ['--domain',   GetoptLong::REQUIRED_ARGUMENT],
      ['--protocol', GetoptLong::REQUIRED_ARGUMENT],
      ['--port',     GetoptLong::REQUIRED_ARGUMENT],
    ]
  end

  def encoding_print_string
    "domain: #{self.domain}, protocol: #{self.protocol}, port: #{self.port}"
  end

end

SanUriGenerator.new.run