#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_hash_encoder.rb'
require 'sha3'

class SHA3_256_Encoder < HashEncoder
  def initialize
    super
    self.module_name = 'SHA3-256 Encoder'
  end

  def hash_function(string)
    SHA3::Digest.hexdigest(:sha3_256, string)
  end
end

SHA3_256_Encoder.new.run