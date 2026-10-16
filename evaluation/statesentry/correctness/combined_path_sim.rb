# Predict the combined_path that StateSentry's Logstash stage computes for each
# correctness case, using the deployed functions in combined_path.rb.
#
#   ruby combined_path_sim.rb ../../../modules/services/unix/logging/logstash/files/combined_path.rb
#
# Assumes Auditbeat's file.path is the PATH record name as passed to open(2),
# and process.working_directory is the kernel-resolved cwd. A case is
# predicted to match the readable_shadow goal when the result is /etc/shadow
# (and the reader is in the filter's executable list).
load ARGV[0]
W = '/home/challenger/.sscorrect'
def cp(cwd, path)
  c = remove_duplicates(cwd + '/' + path)
  remove_relative(c)
end
# /home/challenger: the runner's cwd for each case since cc831e90f, and the
# SSH login directory used by the latency benchmark. / is what a case would
# inherit under the QEMU guest agent without that fix.
runner_cwds = ['/home/challenger', '/']
cases = [
  ['P01', nil, '/etc/shadow'],
  ['P02', '/etc', 'shadow'],
  ['P03', '/home/challenger', '../../etc/shadow'],
  ['P04', nil, '/etc/./shadow'],
  ['P05', nil, '//etc//shadow'],
  ['P06', nil, '/etc/../etc/shadow'],
  ['P07', nil, "#{W}/file_link"],
  ['P08', '/etc', 'shadow'],               # kernel cwd after cd dir_link resolves to /etc
  ['P09', nil, '/etc/shadow'],             # opened by bash
  ['N01', nil, "#{W}/decoy/etc/shadow"],
  ['N02', "#{W}/decoy/etc", 'shadow'],
  ['N03', "#{W}/decoy", 'etc/shadow'],
  ['N04', nil, '/etc/shadow'],
  ['N05', nil, '/etc/shadow'],
  ['N09', nil, 'shadow'],                  # grep -r uses openat(dirfd, "shadow")
  ['N10', nil, '/etc/passwd'],
  ['X1 decoy .//', "#{W}/decoy", './/etc/shadow'],
  ['X2 decoy symlink+..', W, 'L/../../../../etc/shadow'],
  ['X3 decoy x//etc', "#{W}/decoy", 'x//etc/shadow'],
]
runner_cwds.each do |rc|
  puts "== runner cwd #{rc}"
  cases.each do |id, cwd, path|
    r = cp(cwd || rc, path)
    printf("%-20s cwd=%-34s path=%-32s -> %-40s %s\n", id, cwd || rc, path, r, r == '/etc/shadow' ? 'MATCH' : '-')
  end
end
