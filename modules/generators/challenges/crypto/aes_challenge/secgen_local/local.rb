#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_string_encoder.rb'
require 'openssl'

class AESChallenge < StringEncoder
  attr_accessor :key
  attr_accessor :iv

  def initialize
    super
    self.module_name = 'AES Challenge Generator'
    self.key = ''
    self.iv = ''
  end

  def encode(str)

    challenge = {}

    # key_pair = RSA::KeyPair.generate(60)
    #
    # e = key_pair.public_key.exponent
    # n = key_pair.public_key.modulus
    # c = key_pair.encrypt(value)
    #
    # output_data += "e: #{e}\n"
    # output_data += "n: #{n}\n"
    # output_data += "ciphertext: #{c}\n"


    self.outputs << JSON.encode(challenge)
  end

  def get_options_array
    super + [['--message', GetoptLong::REQUIRED_ARGUMENT],
     ['--key', GetoptLong::REQUIRED_ARGUMENT],
     ['--iv', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
      # Removes any non-alphabet characters
    when '--message'
      self.message << arg
    when '--key'
      self.key = arg
    when '--iv'
      self.iv = arg
    else
      # do nothing
    end
  end
end

AESChallenge.new.run