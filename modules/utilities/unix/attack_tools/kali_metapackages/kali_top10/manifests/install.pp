class kali_top10::install{
  # package { ['kali-tools-top10', 'nfs-common', 'ftp']:
  #   ensure => 'installed',
  # }
  ensure_packages(['nfs-common', 'tnftp'])

  # The following is a workaround for the crackmapexec package triggering our IPS
  # almost everything in kali-tools-top10:
  ensure_packages(['aircrack-ng', 'burpsuite', 'hydra', 'john', 'metasploit-framework', 'nmap', 'responder', 'sqlmap', 'wireshark'])
  # except crackmapexec
}
