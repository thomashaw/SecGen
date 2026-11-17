# Installs the Debian Mosquitto broker (2.0 on bookworm) and the command line clients.
class mosquitto::install {
  ensure_packages(['mosquitto', 'mosquitto-clients'])
}
