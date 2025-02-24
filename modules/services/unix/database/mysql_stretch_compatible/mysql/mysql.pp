class { '::mysql::server':
  override_options => {
    'mysqld' => {
      'ssl'         => undef,
      'ssl-ca'      => undef,
      'ssl-cert'    => undef,
      'ssl-key'     => undef,
      'ssl-disable' => true
    }
  }
}

include '::mysql::client'
