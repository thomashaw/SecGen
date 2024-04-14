# keepass/manifests/config.pp

class keepass::config {
    $leaked_passwords = ['password1', 'password2']
    $leaked_usernames = ['user1', 'user2']
    $known_password = 'securepassword'

    file { 'C:\\Users\\timbo_win7\\Desktop\\modules\\keepass\\files\\sample.kdbx':
        ensure  => present,
        source  => 'puppet:///modules/keepass/sample.kdbx',
        require => Package['keepass'],
        notify  => Exec['add_entry_to_keepass'],
    }

    exec { 'add_entry_to_keepass':
        command     => 'powershell.exe -ExecutionPolicy Bypass -File "C:\\Users\\timbo_win7\\Desktop\\modules\\keepass\\Add-KeepassEntry.ps1" -DatabasePath "C:\\Users\\timbo_win7\\Desktop\\modules\\keepass\\files\\sample.kdbx" -MasterPassword "securepassword" -Username "user1" -Password "password1"',
        provider    => 'powershell',
        refreshonly => true,
        subscribe   => File['C:\\Users\\timbo_win7\\Desktop\\modules\\keepass\\files\\sample.kdbx'],
    }
}
