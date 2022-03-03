class kali_pwtools::install{
  # package { ['kali-tools-passwords']:
  #   ensure => 'installed',
  # }

  # The following is a workaround for the mimikatz package triggering our IPS
  # almost everything in kali-tools-passwords:
  ensure_packages(['cewl','chntpw','cisco-auditing-tool','cmospwd','crackle','creddump7','crunch','fcrackzip','freerdp2-x11','gpp-decrypt','hash-identifier','hashcat','hashcat-utils','hashid','hydra','hydra-gtk','john','johnny','kali-tools-gpu','maskprocessor','medusa','ncrack','onesixtyone','ophcrack','ophcrack-cli','pack','passing-the-hash','patator','pdfcrack','pipal','polenum','rainbowcrack','rarcrack','rcracki-mt','rsmangler','samdump2','seclists','sipcrack','sipvicious','sqldict','statsprocessor','sucrack','thc-pptp-bruter','truecrack','twofi','wordlists'])
  # except mimikatz, smbmap

}
