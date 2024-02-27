class easyftp_rce::install {
  $edb_app_path = "http://www.exploit-db.com/apps"
  $mirror_app_path = "http://schreuders.org/exploitdb-apps-mirror"
  $filename = "cf7a11d305a1091b71fe3e5ed5b6a55c-easyftpsvr-1.7.0.2.zip"
  $zipfile = "C:/easyftp.zip"
  $install_path = "C:/Users/vagrant/Downloads/easyftp"

# (new-object System.Net.WebClient).DownloadFile( 'https://hacktivity.aet.leedsbeckett.ac.uk/files/exploit-db-apps/cf7a11d305a1091b71fe3e5ed5b6a55c-easyftpsvr-1.7.0.2.zip', 'C:/Users/vagrant/Downloads/easyftp.zip')
  # file { 'C:/Users/vagrant/Downloads/easyftp.zip':
  #   ensure => present,
  #   source => ["$mirror_app_path/cf7a11d305a1091b71fe3e5ed5b6a55c-easyftpsvr-1.7.0.2.zip",
  #              "$edb_app_path/cf7a11d305a1091b71fe3e5ed5b6a55c-easyftpsvr-1.7.0.2.zip"],
  #  } ->

  exec {'fetch easyftp':
    command => "(new-object System.Net.WebClient).DownloadFile( '$edb_app_path/$filename', '$zipfile'); (new-object System.Net.WebClient).DownloadFile( '$mirror_app_path/$filename', '$zipfile'); \$true ",
    # command => "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; Invoke-WebRequest -Uri \"$mirror_app_path\" -OutFile \"$install_path\" ",
    provider     => 'powershell',
    creates => "$zipfile",
    logoutput => true,
  }->
  # TODO: puppet fail if not created by this point

  file { "$install_path":
    ensure => 'directory',
  } ->

  package { "7zip.portable":
    ensure => installed,
    provider => 'chocolatey',
  } ->

  exec { "&7z e $zipfile -o$install_path -y":
    provider     => 'powershell',
    creates => "$install_path/Ftpconsole.exe",
    logoutput => true,
  } ->

  # run service on boot
  exec { 'schtasks /create /rl HIGHEST /ru system /sc ONSTART /tn easyftp /f /tr C:\Users\vagrant\Downloads\easyftp\Ftpconsole.exe ':
    provider     => 'powershell',
    logoutput => true,
  } ->

  # allow this ftp server program through the firewall
  exec { 'netsh advfirewall firewall add rule name=easyftp dir=in action=allow program=C:\Users\vagrant\Downloads\easyftp\ftpbasicsvr.exe enable=yes':
    provider     => 'powershell',
    logoutput => true,
  } ->
  # improve reliability by adding the firewall rule (again) everytime the VM boots -- messy but works?
  exec { 'schtasks /create /rl HIGHEST /ru system /sc ONSTART /tn easyftpfirewallsch /f /tr "netsh advfirewall firewall add rule name=easyftp dir=in action=allow program=C:\Users\vagrant\Downloads\easyftp\ftpbasicsvr.exe enable=yes" ':
    provider     => 'powershell',
    logoutput => true,
  } ->
  exec { 'runonce_easyftp_firewall':
    command => 'reg add HKLM\SOFTWARE\Microsoft\Windows\CurrentVersion\RunOnce /v effirewallro /t REG_SZ /d "netsh advfirewall firewall add rule name=easyftp dir=in action=allow program=C:\\Users\\vagrant\\Downloads\\easyftp\\ftpbasicsvr.exe enable=yes" /f',
    path    => 'C:\Windows\System32',
    provider => 'powershell',
  }




}
