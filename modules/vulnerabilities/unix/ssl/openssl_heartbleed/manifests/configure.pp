class openssl_heartbleed::configure {

  include openssl

  openssl::certificate::x509 { 'ssl-cert-snakeoil':
    commonname => 'example',
    country => 'UK',
    organization => 'this one',
  }

}