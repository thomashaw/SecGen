class logstash::install () {
  package { 'logstash':
    ensure => present,
  }

  file { '/etc/logstash/combined_path.rb':
    ensure => file,
    source => 'puppet:///modules/logstash/combined_path.rb',
    require => Package['logstash'],
  }

  exec { 'install-logstash-output-exec':
    command => '/usr/share/logstash/bin/logstash-plugin install logstash-output-exec',
    require => Package['logstash'],
    timeout => 600,
  }
}
