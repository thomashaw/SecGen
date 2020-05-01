# auditbeat::repo
# @api private
#
# @summary It manages the package repositories to isntall auditbeat
class auditbeat::repo {
  if ($auditbeat::manage_repo == true) and ($auditbeat::ensure == 'present') {
    notice('auditbeat::repo - Managing and present')
    $family = $facts['osfamily']
    notice("auditbeat::repo - facts[\'osfamily\']::: $family")
    case $facts['osfamily'] {
      'Debian': {
        notice("auditbeat::repo - facts[\'osfamily\']::: $family")
        include ::apt

        $download_url = 'https://artifacts.elastic.co/packages/6.x/apt'

        if !defined(Apt::Source['beats']) {
          notice('auditbeat::repo - installing beats...')
          apt::source{'beats':
            ensure   => $auditbeat::ensure,
            location => $download_url,
            release  => 'stable',
            repos    => 'main',
            key      => {
              id     => '46095ACC8548582C1A2699A9D27D666CD88E42B4',
              source => 'https://artifacts.elastic.co/GPG-KEY-elasticsearch',
            },
          }->
          exec { 'post-source-apt-update':
            command => "/usr/bin/apt-get update --fix-missing",
            tries => 5,
            try_sleep => 30,
          }

        }
      }
      'RedHat': {

        $download_url = 'https://artifacts.elastic.co/packages/6.x/yum'

        if !defined(Yumrepo['beats']) {
          yumrepo{'beats':
            ensure   => $auditbeat::ensure,
            descr    => 'Elastic repository for 6.x packages',
            baseurl  => $download_url,
            gpgcheck => 1,
            gpgkey   => 'https://artifacts.elastic.co/GPG-KEY-elasticsearch',
            enabled  => 1,
          }
        }
      }
      'SuSe': {

        $download_url = 'https://artifacts.elastic.co/packages/6.x/yum'

        exec { 'topbeat_suse_import_gpg':
          command => '/usr/bin/rpmkeys --import https://artifacts.elastic.co/GPG-KEY-elasticsearch',
          unless  => '/usr/bin/test $(rpm -qa gpg-pubkey | grep -i "D88E42B4" | wc -l) -eq 1 ',
          notify  => [ Zypprepo['beats'] ],
        }
        if !defined (Zypprepo['beats']) {
          zypprepo{'beats':
            baseurl     => $download_url,
            enabled     => 1,
            autorefresh => 1,
            name        => 'beats',
            gpgcheck    => 1,
            gpgkey      => 'https://artifacts.elastic.co/GPG-KEY-elasticsearch',
            type        => 'yum',
          }
        }
      }
      default: {
      }
    }
  }
}
