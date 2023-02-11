function apache_spark_rce::cpandbuild(Array $collection, String $filename) >> Undef {
  $collection.each |String $item| {
    file { "/tmp/${item}":
      ensure => file,
      source => "puppet:///modules/apache_spark_rce/${item}",
    }
  }
  exec { "rebuild-${filename}":
    cwd     => '/tmp/',
    command => "cat ${filename}.parta* >${filename}",
  }
}
