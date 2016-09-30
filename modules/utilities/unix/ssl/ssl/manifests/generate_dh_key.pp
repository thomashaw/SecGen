# == Define: ssl::generate_dh_key
#
# Generate a Diffie-Hellmann key.
# Output of $directory/$title.pem is created.
#
# This works on Debian and RedHat like systems.
# Puppet Version >= 3.4.0
#
# === Parameters
#
# [*numbits*]
#   Number of bits for parameter set.
#   *Optional* (defaults to 512)
#
# [*generator*]
#   Generator to use, valid 2 or 5.
#   *Optional* (defaults to 2)
#
# [*directory*]
#   Were to put the resulting files.
#   *Optional* (defaults to /etc/ssl)
#
# [*owner*]
#   Owner of files.
#   *Optional* (defaults to root)
#
# [*group*]
#   Group of files.
#   *Optional* (defaults to root)
#
# === Examples
#
# ssl::generate_dh_key{ 'dh_512': }
#
# === Authors
#
# Frederik Wagner <wagner@wagit.de>
#
# === Copyright
#
# Copyright 2014 Frederik Wagner
#
define ssl::generate_dh_key (
  $numbits          = '512',
  $generator        = '2',
  $directory        = '/etc/ssl',
  $owner            = root,
  $group            = root,
) {
  validate_re($numbits, ['^\d+$'])
  validate_re($generator, ['^2$','^5$'])
  validate_absolute_path($directory)
  validate_string($owner)
  validate_string($group)

  include ssl::install

  # basename for key
  $basename = "${directory}/${name}"

  ensure_resource('file', $directory, { ensure => directory })

  # create Diffie-Hellmann key
  exec {"create DH key ${name}.pem":
    command => "openssl dhparam -out ${basename}.pem -${generator} ${numbits}",
    creates => "${basename}.pem",
    path    => ['/usr/bin'],
    before  => File["${basename}.pem"],
  }
  file {"${basename}.pem":
    mode  => '0644',
    owner => $owner,
    group => $group,
  }

}
