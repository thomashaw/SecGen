# == Class: ssl
#
# This class creates a self sigend cretificate.
# You can choose, if it should be a wildcard
# certificate.
#
# This works on Debian and RedHat like systems.
# Puppet Version >= 3.4.0
#
# === Parameters
#
# [*cert_name*]
#   Name of the files (basename).
#   *Optional* (defaults to $::fqdn)
#
# [*directory*]
#   Where to store the files.
#   *Optional* (defaults to /tmp).
#
# [*wildcard*]
#   If a wildcard certificate should be created.
#   *Optional* (defaults to false)
#
# === Examples
#
# include ssl
#
# === Authors
#
# Frederik Wagner <wagner@wagit.de>
#
# === Copyright
#
# Copyright 2014 Frederik Wagner
#
class ssl (
  $cert_name = $::fqdn,
  $directory = '/tmp',
  $wildcard  = false,
) {

  validate_string($cert_name)
  validate_absolute_path($directory)

  $cn_split = split($cert_name,'\.')
  $domain   = join(delete_at($cn_split, 0), '.')

  # take last part as country (only works for 2 lettered TLDs)
  if member(['COM','ORG','NET'], upcase($cn_split[-1])) {
    $country = 'US'
  } else {
    $country = upcase($cn_split[-1])
  }
  # take second part to be organization (such that .co.uk etc work as well)
  $organization  = $cn_split[1]
  $email_address = "root@${domain}"

  if str2bool($wildcard) {

    ssl::self_signed_certificate { $cert_name:
      common_name      => $cert_name,
      email_address    => $email_address,
      country          => $country,
      organization     => $organization,
      subject_alt_name => "DNS:*.${::domain}, DNS:${::domain}",
      directory        => $directory,
    }

  } else {

    ssl::self_signed_certificate { $cert_name:
      common_name   => $cert_name,
      email_address => $email_address,
      country       => $country,
      organization  => $organization,
      directory     => $directory,
    }
  }


}
