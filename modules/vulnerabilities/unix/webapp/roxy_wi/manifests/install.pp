class roxy_wi::install {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $strings_to_leak = $secgen_parameters['strings_to_leak']
  $leaked_filenames = $secgen_parameters['leaked_filenames']

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  # Packages needed for the service to run and for the vulnerable CGI endpoint
  # (app/options.py) to import cleanly on Debian 12 / Python 3.11. The import
  # chain for the RCE sink is requests -> jinja2 -> distro -> peewee plus the
  # stdlib cgi/configparser modules; the heavier Roxy-WI features (ansible,
  # matplotlib, networkx, paramiko) are not on that path and are intentionally
  # left out to keep the build fast and to avoid apt-name churn. netcat is a
  # virtual package on bookworm, so the concrete netcat-traditional is used.
  ensure_packages(['apache2', 'unzip', 'openssl', 'dos2unix', 'net-tools',
                   'netcat-traditional', 'sshpass', 'libapache2-mod-wsgi-py3',
                   'python3', 'python3-pip', 'python3-requests', 'python3-jinja2',
                   'python3-distro', 'python3-peewee', 'python3-pymysql'])

  # Bundled release: v6.1.0.0 (confirmed vulnerable to CVE-2022-31137; the
  # June-2022 6.1.1.0 release fixed the app/options.py injection sink this
  # module's secgen_test exploits). Its zip's internal top-level folder is
  # roxy-wi-6.1.0.0/, not roxy-wi-master/.
  file { '/var/www/roxy-wi-6.1.0.0.zip':
    source => 'puppet:///modules/roxy_wi/roxy-wi-6.1.0.0.zip',
    owner  => 'www-data',
    mode   => '0644',
  } ->

  exec { 'unzip-roxy-wi':
    command => 'unzip -o roxy-wi-6.1.0.0.zip',
    cwd     => '/var/www',
    creates => '/var/www/haproxy-wi',
  } ->

  exec { 'rename-to-haproxy-wi':
    command => 'bash -c \'d=$(find /var/www -maxdepth 1 -type d -name "roxy-wi-*" ! -name "haproxy-wi" | head -n1); if [ -n "$d" ]; then mv "$d" /var/www/haproxy-wi; fi\'',
    creates => '/var/www/haproxy-wi',
  } ->

  # GitHub's codeload archive (the source this module is bundled from) stores
  # every file with zero permission bits, so the extracted app/*.py scripts
  # lose their executable bit. The vhost below serves them as CGI
  # (AddHandler cgi-script .py); without +x Apache 500s on them instead of
  # running them, including the vulnerable app/options.py. Restore it.
  exec { 'chmod-roxy-wi-cgi-scripts':
    command => 'find /var/www/haproxy-wi/app -maxdepth 1 -name "*.py" -exec chmod 755 {} +',
    unless  => 'test -x /var/www/haproxy-wi/app/options.py',
  } ->

  # Roxy-WI ships its Apache vhost (an HTTPS/:443 site serving the Python files
  # as CGI). Install it, enable it and enable the modules it needs.
  exec { 'install-roxy-wi-apache-site':
    command => 'cp /var/www/haproxy-wi/config_other/httpd/roxy-wi_deb.conf /etc/apache2/sites-available/roxy-wi.conf',
    creates => '/etc/apache2/sites-available/roxy-wi.conf',
  } ->

  exec { 'enable-roxy-wi-site':
    command => 'a2ensite roxy-wi.conf',
  } ->

  exec { 'enable-apache-modules':
    command => 'a2enmod cgid ssl proxy proxy_http rewrite headers wsgi',
  } ->

  # Config + state directories the application reads/writes at runtime.
  file { ['/var/lib/roxy-wi',
          '/var/lib/roxy-wi/keys',
          '/var/lib/roxy-wi/configs',
          '/var/lib/roxy-wi/configs/hap_config',
          '/var/lib/roxy-wi/configs/kp_config',
          '/var/lib/roxy-wi/configs/nginx_config',
          '/var/lib/roxy-wi/configs/apache_config',
          '/var/www/haproxy-wi/log',
          '/var/www/haproxy-wi/app/certs',
          '/etc/roxy-wi']:
    ensure => directory,
  } ->

  # roxy_wi_tools.GetConfigVar reads /etc/roxy-wi/roxy-wi.cfg; the sqlite DB
  # created below depends on it existing first. In v6.1.0.0 this ships at
  # app/roxy-wi.cfg (not at the archive's top level, as the later master
  # branch had it), hence the app/ prefix here.
  exec { 'install-roxy-wi-config':
    command => 'cp /var/www/haproxy-wi/app/roxy-wi.cfg /etc/roxy-wi/roxy-wi.cfg',
    creates => '/etc/roxy-wi/roxy-wi.cfg',
  } ->

  # Install the small pure-Python requirements the app expects beyond the apt
  # packages above. Debian 12 marks the system environment externally managed
  # (PEP 668), so --break-system-packages is required for a system-wide pip
  # install. Non-fatal: these packages are not on the RCE import path, so a pip
  # hiccup must not fail provisioning.
  exec { 'install-python-requirements':
    command => 'pip3 install --break-system-packages -r /var/www/haproxy-wi/config_other/requirements_deb.txt || true',
  } ->

  # Self-signed certificate for the HTTPS vhost.
  exec { 'generate-tls-cert':
    command => 'openssl req -newkey rsa:4096 -nodes -keyout /var/www/haproxy-wi/app/certs/haproxy-wi.key -x509 -days 10365 -out /var/www/haproxy-wi/app/certs/haproxy-wi.crt -subj "/C=US/ST=Almaty/L=Springfield/O=Roxy-WI/OU=IT/CN=*.roxy-wi.org"',
    creates => '/var/www/haproxy-wi/app/certs/haproxy-wi.crt',
  } ->

  # Bootstrap the sqlite database (settings table drives the vulnerable code
  # path, e.g. haproxy_sock_port). Runs from the app directory so its intra-app
  # imports resolve. Non-fatal so a single migration quirk does not abort the
  # whole build; the service and lower tiers still stand.
  exec { 'create-roxy-wi-database':
    command => 'python3 create_db.py || true',
    cwd     => '/var/www/haproxy-wi/app',
    creates => '/var/lib/roxy-wi/roxy-wi.db',
  } ->

  exec { 'own-www':
    command => 'chown -R www-data:www-data /var/www/haproxy-wi',
  } ->

  exec { 'own-lib':
    command => 'chown -R www-data:www-data /var/lib/roxy-wi',
  } ->

  exec { 'enable-apache2':
    command => 'systemctl enable apache2',
  } ->

  exec { 'restart-apache2':
    command => 'systemctl restart apache2',
  }

  # Post-exploitation objective: a flag placed outside the web root
  # (/var/www, not the /var/www/haproxy-wi DocumentRoot) so it cannot be fetched
  # over HTTP and only the www-data command execution the RCE grants can read it.
  ::secgen_functions::leak_files { 'roxy_wi':
    storage_directory => '/var/www',
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    owner             => 'www-data',
    group             => 'www-data',
    mode              => '0640',
    leaked_from       => 'roxy_wi',
  }
}
