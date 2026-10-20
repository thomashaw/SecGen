#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_encoder.rb'
require 'open3'
require 'tmpdir'
require 'timeout'

class GPGKeyPairGenerator < StringEncoder
  GPG_TIMEOUT = 120

  def initialize
    super
    self.module_name = 'GPG Key Pair Generator'
  end

  def encode_all
    params_file = "#{GENERATORS_DIR}crypto/gpg_key_pair/files/parameterfile"
    # The key is unprotected: the challenge hands the student the private key.
    # Without %no-protection, gpg asks gpg-agent for a passphrase and blocks.
    params = File.read(params_file).chomp + "\n%no-protection\n%commit\n"

    # A throwaway keyring per run: keeps the host's ~/.gnupg clean and lets
    # parallel builds generate keys without sharing a keyring or gpg-agent.
    Dir.mktmpdir('secgen_gpg_') do |gnupghome|
      env = { 'GNUPGHOME' => gnupghome }
      begin
        gpg(env, %w[--gen-key], params)
        fingerprint = gpg(env, %w[--with-colons --list-secret-keys]).lines
                        .find { |l| l.start_with?('fpr:') }&.split(':')&.at(9)
        raise 'gpg generated no secret key' if fingerprint.nil? || fingerprint.empty?

        private_key = gpg(env, ['--armor', '--export-secret-keys', fingerprint])
        public_key = gpg(env, ['--armor', '--export', fingerprint])
        raise 'gpg exported an empty key' if private_key.empty? || public_key.empty?

        self.outputs << { 'private' => private_key, 'public' => public_key }.to_json
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
end

GPGKeyPairGenerator.new.run
