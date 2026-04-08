#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_encoder.rb'
require 'securerandom'

class SanUriGenerator < StringEncoder

  def initialize
    super
    self.module_name = 'SAN URI Generator'
  end

  def encode_all
    domain   = self.get_inputs['domain'].first || 'localhost'
    protocol = self.get_inputs['protocol'].first || 'http'
    port     = self.get_inputs['port'].first

    # Generate a random hex path — 16 bytes = 32 hex chars, unguessable
    hex_path = SecureRandom.hex(16)

    # Build URI — omit port if not supplied or empty
    if port && !port.empty?
      uri = "#{protocol}://#{domain}:#{port}/#{hex_path}/"
    else
      uri = "#{protocol}://#{domain}/#{hex_path}/"
    end

    self.outputs << uri
  end

end

SanUriGenerator.new.run