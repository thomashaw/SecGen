# Installs the Debian tftpd-hpa server.
class tftpd_hpa::install {
  ensure_packages(['tftpd-hpa'])
}
