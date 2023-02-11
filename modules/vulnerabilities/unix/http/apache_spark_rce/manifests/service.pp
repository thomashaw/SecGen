# Class: apache_spark_rce::service
# Service to start spark-shell
#
class apache_spark_rce::service {
  file { '/etc/systemd/system/spark.service':
    source => 'puppet:///modules/apache_spark_rce/spark.service',
    owner  => 'root',
    mode   => '0777',
  }

  service { 'spark':
    ensure => running,
    enable => true,
  }
}
