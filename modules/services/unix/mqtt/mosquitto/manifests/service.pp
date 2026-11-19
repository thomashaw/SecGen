# Enables the Mosquitto service at boot, restarting it when its configuration changes.
class mosquitto::service {
  service { 'mosquitto':
    ensure    => running,
    enable    => true,
    require   => Class['mosquitto::install'],
    subscribe => [File['/etc/mosquitto/mosquitto.conf'], File['/etc/mosquitto/passwd']],
  }
}
