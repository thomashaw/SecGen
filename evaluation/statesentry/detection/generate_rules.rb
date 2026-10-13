# Print the Auditbeat watch rule and ElastAlert rule that SecGen generates for
# experiment_aaa's readable_shadow goal, using a given copy of rules.rb.
#
#   ruby generate_rules.rb before/rules.rb > before/readable_shadow_rules.txt
#   ruby generate_rules.rb ../../../lib/helpers/rules.rb > after/readable_shadow_rules.txt
#
# rules.rb requires print.rb and scenario.rb relative to itself, so a snapshot is
# loaded from inside lib/helpers by temporarily resolving those paths there.
require 'pathname'
helpers = File.expand_path('../../../lib/helpers', __dir__)
src = File.read(ARGV[0]).gsub("require_relative './", "require '#{helpers}/")
eval(src, binding, ARGV[0])

goal = { 'goal_type' => 'read_file', 'file_path' => '/etc/shadow',
         'hostname' => 'tom-noekea-2-experiment-aaa-hackme-desktop' }
goal['read_by'] = ARGV[1] if ARGV[1]

puts '# Auditbeat rule (audit.rules.d)'
puts Rules.generate_auditbeat_rules([goal])
puts
puts '# ElastAlert rule'
puts Rules.generate_elastalert_rule(goal['hostname'], 'readable_shadow', goal, 0)
