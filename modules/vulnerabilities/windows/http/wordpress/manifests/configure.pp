class wordpress::configure {
  # configure_firewall.bat:
  #   netsh advfirewall firewall add rule name="Open Port 8585 for Wordpress and phpMyAdmin" dir=in action=allow protocol=TCP localport=8585
}

