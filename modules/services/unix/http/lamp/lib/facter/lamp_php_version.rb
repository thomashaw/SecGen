# The PHP version Debian's (or Kali's) default `php` package installs, e.g.
# "8.2" on bookworm or "7.3" on buster, from the candidate version of the
# metapackage ("2:8.2+93"). lamp::config uses it to name mod_php's package, its
# library (libphp<version>.so) and its .load/.conf files.
Facter.add(:lamp_php_version) do
  confine osfamily: 'Debian'
  setcode do
    out = Facter::Core::Execution.execute('apt-cache policy php 2>/dev/null', on_fail: nil).to_s
    out[/Candidate:\s*(?:\d+:)?(\d+\.\d+)\+/, 1]
  end
end
