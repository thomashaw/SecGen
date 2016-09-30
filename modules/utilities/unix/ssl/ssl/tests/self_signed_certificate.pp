ssl::self_signed_certificate {'test_cert':
  directory        => '/tmp',
  subject_alt_name => "DNS:*.${::domain}, DNS:${::domain}",
}
