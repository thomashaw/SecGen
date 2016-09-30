class openssl_heartbleed::configure {

  include ssl# todo: test this commented out

  ssl::self_signed_certficate { 'ssl.example.com':
    common_name   => 'ssl.example.com',
    email_address    => 'root@example.com',
    country          => 'US',
    organization     => 'Example',
    days             => 730,
    directory        => '/etc/ssl/',
    subject_alt_name => "DNS:*.${::domain}, DNS:${::domain}",
  }

}