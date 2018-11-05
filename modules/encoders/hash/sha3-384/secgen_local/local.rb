#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_hash_encoder.rb'

class SHA3_384_Encoder < HashEncoder
  def initialize
    super
    self.module_name = 'SHA3-384 Encoder'
  end

  def hash_function(string)
    Digest::SHA3.hexdigest('foo', 384)
  end
end

SHA3_384_Encoder.new.run