class apache_from_source {
  class { '::apache_from_source::vulnerabilities': } ->
  class { '::apache_from_source::install': }
}