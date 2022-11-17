#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'open3'
require 'securerandom'

class AESKeyByteStrGenerator < StringGenerator

  def initialize
    super
    self.module_name = 'AES Key Byte String Generator'
  end

  def generate
    # self.outputs << 'b"' + SecureRandom.hex(8) + '"'
    self.outputs << 'b"ThisIsASecretKey"'
  end
end

AESKeyByteStrGenerator.new.run
