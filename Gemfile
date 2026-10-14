source 'https://rubygems.org'

#production gems go here
# Supported Ruby versions: 2.7 (Ubuntu 20.04) to 4.0. Gemfile.lock is resolved under
# Ruby 2.7 so the same lock installs unchanged on Ruby 2.7 - 3.4 (Ubuntu 20.04 - 26.04);
# Ruby 4.0 re-resolves a few gems locally on first `bundle install`.
# Regenerate with `bundle lock --update` under Ruby 2.7, then delete the BUNDLED WITH
# section: if it is present, newer Bundlers switch to that (old) Bundler, which breaks
# on Ruby 4.0. See docker/README.md for the test containers.

# default gems that newer Rubies no longer bundle (3.4+); harmless on older Rubies
gem 'base64'
gem 'csv'
gem 'getoptlong'
gem 'logger'
# forwardable 1.4 uses `(arg, ...)` syntax, which Ruby 2.7.0-2.7.2 (Ubuntu 20.04) cannot parse
gem 'forwardable', '< 1.4'
gem 'nokogiri'
gem 'puppet'
# >= 5: on Ruby 4 the lock can't be used as-is (librarian-puppet 6 is capped at ruby < 4),
# and without this bundler falls back to librarian-puppet 1.x instead of 7.x
gem 'librarian-puppet', '>= 5'
gem 'wordlist'
gem 'faker'
gem 'forgery'
gem 'redcarpet'
gem 'rqrcode'
gem 'mini_exiftool_vendored'
gem 'sshkey'
gem 'zip-zip'
gem 'credy'
gem 'pg'
gem 'cinch'
gem 'nori'
gem 'bcrypt'
gem 'programr', :git => "https://github.com/robertjwhitney/programr.git"
gem 'process_helper'
gem 'ovirt-engine-sdk'
gem 'duplicate'
gem 'smbhash'
gem 'digest-siphash'
gem 'scrypt'
gem 'braille', :git => "https://github.com/nicanor/braille.git"
gem 'bases'
gem 'priority_queue', :git => "https://github.com/fgosew/priority_queue"
gem 'huffman', :git => "https://github.com/fgosew/huffman"
gem 'ruby-graphviz'
gem 'rsa'
gem 'gpgmeh'
# 2.x API: the sha3 encoders use SHA3::Digest.hexdigest(:sha3_256, ...); 1.x took :sha256
gem 'sha3', '~> 2.0'
gem 'packetfu'
gem 'net-ntp'
# 3.0.9 declares ruby < 3.2 (4.x needs ruby >= 3.2); 3.0.8 installs on 2.7 to 4.0
gem 'CFPropertyList', '~> 3.0', '!= 3.0.9'
gem 'artii'
gem 'rest-client'
gem 'retryable'
gem 'sqlite3'

#development only gems go here
group :test, :development do
  gem 'minitest'
  gem 'rake'
  gem 'yard'
end
