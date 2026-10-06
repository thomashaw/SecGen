# Loaded with `ruby -r` before a hackerbot_config generator's local.rb so it can
# run outside `bundle exec` on a dev machine. Changes nothing when the
# environment is already right.
#
# HackerbotConfigGenerator#generate calls ERB.new(str, 0, '<>-'). The erb 4.x
# that ships with Ruby (what `bundle exec` resolves to, as erb isn't in
# Gemfile.lock) accepts that; a newer erb gem (>= 5) installed system-wide
# raises ArgumentError instead.
require 'erb'
begin
  ERB.new('', 0, '<>-')
rescue ArgumentError
  class ERB
    alias_method :__hb_initialize, :initialize
    def initialize(str, legacy_safe = nil, legacy_trim = nil, legacy_eout = nil, **kw)
      kw[:trim_mode] ||= legacy_trim if legacy_trim
      kw[:eoutvar] ||= legacy_eout if legacy_eout
      __hb_initialize(str, **kw)
    end
  end
end
