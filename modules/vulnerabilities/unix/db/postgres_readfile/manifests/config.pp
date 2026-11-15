# Configure the PostgreSQL arbitrary-file-read misconfiguration.
#
# The required postgresql service module installs, configures and starts the
# cluster (bound to localhost). This class layers the misconfiguration on top:
# it exposes the cluster on every interface, appends a pg_hba.conf rule so a
# dedicated login role can authenticate from the network, grants that role the
# predefined pg_read_server_files role (which lets COPY ... FROM read any file
# the database process can reach) and plants a server-readable flag file.
# Each SecGen module is applied as its own separate puppet run, so this catalog
# cannot reference the service module's classes (Class['postgresql::install'] and
# friends live in a different apply). The required postgresql service module runs
# first — by the time this class is applied the cluster is installed, configured
# (pg_hba.conf, conf.d/) and running — so ordering here is purely internal.
class postgres_readfile::config {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $username          = $secgen_parameters['username'][0]
  $password          = $secgen_parameters['password'][0]
  $database_name     = $secgen_parameters['database_name'][0]
  $auth_method       = $secgen_parameters['auth_method'][0]
  $storage_directory = $secgen_parameters['storage_directory'][0]
  $leaked_filenames  = $secgen_parameters['leaked_filenames']
  $strings_to_leak   = $secgen_parameters['strings_to_leak']

  # Store the password in the form the pg_hba method expects. md5 in pg_hba also
  # accepts a scram-stored password, but storing an md5 hash keeps older
  # Metasploit builds (which only speak md5) working.
  $pw_encryption = $auth_method ? {
    'scram-sha-256' => 'scram-sha-256',
    default         => 'md5',
  }

  $conf_dir  = '/etc/postgresql/15/main'
  $pg_hba    = "${conf_dir}/pg_hba.conf"
  $setup_sql = '/var/lib/postgresql/secgen_readfile_setup.sql'

  # 1. Expose the cluster on every interface. conf.d/*.conf is read in
  # alphabetical order, so this file overrides the service module's
  # localhost-only secgen.conf.
  file { "${conf_dir}/conf.d/secgen_readfile.conf":
    ensure  => file,
    owner   => 'postgres',
    group   => 'postgres',
    mode    => '0644',
    content => "listen_addresses = '*'\n",
  }

  # 2. Allow the remote role to authenticate from any address. The service
  # module owns pg_hba.conf as a whole, so append to it rather than redeclare
  # it. The marker keeps the append idempotent.
  exec { 'secgen-readfile-pg-hba':
    command  => "/usr/bin/printf '# secgen postgres_readfile\\nhost %s %s 0.0.0.0/0 %s\\nhost %s %s ::/0 %s\\n' '${database_name}' '${username}' '${auth_method}' '${database_name}' '${username}' '${auth_method}' >> '${pg_hba}'",
    provider => shell,
    unless   => "/bin/grep -q '# secgen postgres_readfile' '${pg_hba}'",
  }

  # 3. Restart so listen_addresses and pg_hba take effect, then wait for the
  # cluster to accept connections again.
  exec { 'secgen-readfile-restart':
    command     => '/usr/bin/systemctl restart postgresql@15-main',
    refreshonly => true,
    subscribe   => [File["${conf_dir}/conf.d/secgen_readfile.conf"], Exec['secgen-readfile-pg-hba']],
  }

  exec { 'secgen-readfile-wait':
    command   => '/usr/bin/pg_isready -q -p 5432',
    tries     => 15,
    try_sleep => 2,
    require   => Exec['secgen-readfile-restart'],
  }

  # 4. Create the remote login role, grant it pg_read_server_files and create a
  # database it owns. The SQL holds the password, so it is removed once psql has
  # run it.
  file { $setup_sql:
    ensure    => file,
    owner     => 'postgres',
    group     => 'postgres',
    mode      => '0600',
    content   => template('postgres_readfile/readfile_setup.sql.erb'),
    show_diff => false,
    require   => Exec['secgen-readfile-wait'],
  }

  exec { 'secgen-readfile-setup':
    command  => "/usr/sbin/runuser -u postgres -- /usr/bin/psql -p 5432 -v ON_ERROR_STOP=1 -q -f ${setup_sql}; rc=\$?; rm -f ${setup_sql}; exit \$rc",
    provider => shell,
    require  => File[$setup_sql],
  }

  # 5. Plant the flag at a server-readable path. mode 0644 lets the database
  # process (the postgres user) read it; leak_files creates the directory.
  ::secgen_functions::leak_files { 'postgres_readfile_flag':
    storage_directory => $storage_directory,
    leaked_filenames  => $leaked_filenames,
    strings_to_leak   => $strings_to_leak,
    leaked_from       => 'postgres_readfile',
    owner             => 'root',
    group             => 'root',
    mode              => '0644',
  }
}
