class drupal_850::install {
    Exec { path => ['/bin', '/usr/bin', '/sbin', '/usr/sbin']}

    # PHP itself (7.4, with the gd/xml extensions Drupal needs) comes from
    # the required php7 utility module; it provisions first as a <requires>
    # dependency but never touches Apache, so enabling PHP in Apache is this
    # module's own job (manifests/apache.pp). php7 doesn't bundle the MySQL
    # PDO driver (most of its other callers are flat-file apps), so that's
    # added here instead of widening the shared utility.
    ensure_packages(['php7.4-mysql','libaio1','libnuma1'])

    $archive = 'drupal-8.5.0.tar.gz'
    $longmysql = 'mysql-5.7.44-linux-glibc2.12-x86_64.tar.gz' 
    $shortmysql = 'mysql-5.7.44-linux-glibc2.12-x86_64'
    $mysqlpart = ["${longmysql}.partaa",
    "${longmysql}.partab",
    "${longmysql}.partac",
    "${longmysql}.partad",
    "${longmysql}.partae",
    "${longmysql}.partaf",
    "${longmysql}.partag",
    "${longmysql}.partah",
    "${longmysql}.partai",
    "${longmysql}.partaj",
    "${longmysql}.partak",
    "${longmysql}.partal",
    "${longmysql}.partam",
    "${longmysql}.partan",]

    $pkgtobuild = [[$mysqlpart, $longmysql]]
    $pkgtobuild.each |Array $pkg| {
        drupal_850::cpandbuild($pkg[0], $pkg[1])
    }

    #Drupal installation
    file {"/usr/local/src/${archive}":
        ensure => file,
        source => "puppet:///modules/drupal_850/${archive}",
    } ->
    file {"/usr/local/src/drupal_pre_setup.tar.gz":
        ensure => file,
        source => "puppet:///modules/drupal_850/drupal_pre_setup.tar.gz",
    } ->
    file {"/usr/local/src/drupal_pre_setup.sql":
        ensure => file,
        source => "puppet:///modules/drupal_850/drupal_pre_setup.sql",
    } ->
    exec { 'unpack-drupal':
        cwd => '/usr/local/src',
        command => "tar -xzf ${archive} -C /var/www",
    } ->
    exec { 'chown-drupal':
        command => "chown www-data. /var/www/drupal-8.5.0 -R",
    }

    #MySQL installation
    # The upstream mysql.com tarball (unlike the Debian mariadb-server
    # package it replaces) doesn't create a system account; configure.pp
    # needs 'mysql' to own /var/lib/mysql before mysqld's own
    # --initialize-insecure run, so it's created here.
    group { 'mysql':
        ensure => present,
        system => true,
    } ->
    user { 'mysql':
        ensure => present,
        system => true,
        gid => 'mysql',
        home => '/nonexistent',
        shell => '/usr/sbin/nologin',
    } ->
    file { '/etc/systemd/system/mysql.service':
        ensure => file,
        source => "puppet:///modules/drupal_850/config/mysql.service.erb",
    } ->
    exec { 'unpack-mysql':
        cwd => '/tmp/',
        command => "tar -xf ${longmysql}",
        require => Exec["rebuild-${longmysql}"],
        creates => "/tmp/${shortmysql}",
    } ->
    exec { 'move-mysql':
        cwd => '/tmp',
        command => "mv ${shortmysql} /usr/local/mysql",
        creates => '/usr/local/mysql',
    }
}
