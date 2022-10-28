#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_string_encoder.rb'
require 'openssl'
require 'json'

class AESChallenge < StringEncoder
  attr_accessor :key

  def initialize
    super
    self.module_name = 'AES Challenge Generator'
    self.key = ''
  end

  def encode(str)

    # Validation:
    if self.key.length != 32
      Print.err "AES 256 Key length must be 32 characters long"
      exit(1)
    end

    # challenge_file_path = GENERATORS_DIR + 'challenges/crypto/aes_challenge/tmp/challenge/' + Time.new.strftime("%Y%m%d_%H%M%S") + '.zip'

    challenge = {}
    cipher = OpenSSL::Cipher.new("aes-256-cbc")
    cipher.encrypt
    cipher.iv = challenge['iv'] = cipher.random_iv
    challenge['message'] = self.strings_to_encode[0]
    cipher.key = challenge['key'] = self.key
    challenge['ciphertext'] = cipher.update(challenge['message']) + cipher.final

    Print.info "AES Challenge Generator"
    Print.info "  ciphertext: <added>"
    Print.info "  key: <added>"
    Print.info "  iv: <added>"

    b64_utf8_ct = Base64.strict_encode64(challenge['ciphertext']).encode('utf-8')
    b64_utf8_key = Base64.strict_encode64(challenge['key']).encode('utf-8')
    b64_utf8_iv = Base64.strict_encode64(challenge['iv']).encode('utf-8')

    Print.info "  b64_ciphertext: " + b64_utf8_ct
    Print.info "  b64_key: " + b64_utf8_key
    Print.info "  b64_iv: " + b64_utf8_iv

    b64_utf8_challenge = {'key' => b64_utf8_key, 'ciphertext' => b64_utf8_ct, 'iv' => b64_utf8_iv }.to_json

    Print.info "b64_utf8_challenge: " + b64_utf8_challenge.to_s
    # Code to test decoding + decrypting...
    # decoded_ct = Base64.strict_decode64(b64_utf8_ct)
    # decoded_key = Base64.strict_decode64(b64_utf8_key)
    # decoded_iv = Base64.strict_decode64(b64_utf8_iv)
    #
    # decipher = OpenSSL::Cipher.new("aes-256-cbc")
    # decipher.decrypt
    # decipher.key = decoded_key
    # decipher.iv = decoded_iv
    # decrypted = decipher.update(decoded_ct)+ decipher.final


    self.outputs << Base64.strict_encode64(b64_utf8_challenge)
  end

  def get_options_array
    super + [['--message', GetoptLong::REQUIRED_ARGUMENT],
     ['--key', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
      # Removes any non-alphabet characters
    when '--key'
      self.key = arg
    else
      # do nothing
    end
  end
end

AESChallenge.new.run