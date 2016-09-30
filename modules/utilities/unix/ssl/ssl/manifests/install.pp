# installs openssl (private)
class ssl::install {
  ensure_resource('package', 'openssl', { 'ensure' => 'latest' })
}
