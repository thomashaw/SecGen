#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_string_encoder.rb'
require 'json'
require 'open3'
require 'tmpdir'
require 'timeout'

class SimpleGPGDecrypt < StringEncoder
  GPG_TIMEOUT = 120

  attr_accessor :gpg_key_pair
  attr_accessor :subdirectory

  def initialize
    super
    self.module_name = 'Simple GPG Decryption Challenge'
    self.subdirectory = ''
    self.gpg_key_pair = {}
  end

  def encode_all
    public_ascii = self.gpg_key_pair['public'].to_s
    private_ascii = self.gpg_key_pair['private'].to_s
    if public_ascii.empty? || private_ascii.empty?
      raise 'simple_gpg needs a gpg_key_pair input with both public and private keys'
    end

    # A throwaway keyring per run: the host's ~/.gnupg is never touched, and
    # parallel builds don't share files, a trustdb or a gpg-agent.
    Dir.mktmpdir('secgen_simple_gpg_') do |gnupghome|
      env = { 'GNUPGHOME' => gnupghome }
      begin
        gpg(env, %w[--import], public_ascii)
        fingerprint = gpg(env, %w[--with-colons --list-keys]).lines
                        .find { |l| l.start_with?('fpr:') }&.split(':')&.at(9)
        raise 'the gpg_key_pair public key could not be imported' if fingerprint.nil? || fingerprint.empty?

        plaintext = self.strings_to_encode.map { |line| line + "\n" }.join
        ciphertext = gpg(env, ['--trust-model', 'always', '--armor', '--encrypt',
                               '--recipient', fingerprint], plaintext)

        self.outputs << {:secgen_leaked_data => {:data => Base64.strict_encode64(ciphertext), :filename => 'cipher', :ext => 'txt', :subdirectory => self.subdirectory}}.to_json
        self.outputs << {:secgen_leaked_data => {:data => Base64.strict_encode64(private_ascii), :filename => 'private', :ext => 'key', :subdirectory => self.subdirectory}}.to_json
      ensure
        Open3.capture3(env, 'gpgconf', '--kill', 'gpg-agent')
      end
    end
  end

  # Runs gpg non-interactively and returns stdout; raises with stderr on failure.
  def gpg(env, args, stdin_data = '')
    stdout, stderr, status = Timeout.timeout(GPG_TIMEOUT) do
      Open3.capture3(env, 'gpg', '--batch', '--yes', *args, stdin_data: stdin_data)
    end
    raise "gpg #{args.first} failed: #{stderr}" unless status.success?
    stdout
  end

  def process_options(opt, arg)
    super
    case opt
    when '--subdirectory'
      self.subdirectory << arg;
    when '--gpg_key_pair'
      self.gpg_key_pair = JSON.parse(arg);
    end
  end

  def get_options_array
    super + [['--subdirectory', GetoptLong::REQUIRED_ARGUMENT],
             ['--gpg_key_pair', GetoptLong::REQUIRED_ARGUMENT]]
  end


  def encoding_print_string
    'strings_to_encode: ' + self.strings_to_encode.to_s + print_string_padding +
    'subdirectory: ' + self.subdirectory.to_s + print_string_padding +
    'gpg_key_pair: ' + self.gpg_key_pair.to_json
  end
end

SimpleGPGDecrypt.new.run
