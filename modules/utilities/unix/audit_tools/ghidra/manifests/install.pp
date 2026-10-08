class ghidra::install{

  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }
  ensure_packages('zip')

  if $operatingsystem == 'Kali' {
    # Kali: download the current upstream release at build time (Kali's own ghidra package, 12.1.3+ds, crashes in
    # JavaHelp with "view is invalid"). Ghidra 12.x needs a JDK 21+; pin JDK 21, as kali-rolling defaults to newer.
    $ghidra_version = '12.1.4'
    $ghidra_zip     = 'ghidra_12.1.4_PUBLIC_20260921.zip'
    $ghidra_sha256  = 'ddac49f903da9d5bac833e5cc79395098b9c33cfd3279be5f31bd00387d2d4db'
    $ghidra_url     = "https://github.com/NationalSecurityAgency/ghidra/releases/download/Ghidra_${ghidra_version}_build/${ghidra_zip}"
    $ghidra_dir     = "/opt/ghidra_${ghidra_version}_PUBLIC"

    ensure_packages(['openjdk-21-jre', 'openjdk-21-jdk', 'curl', 'unzip', 'gdb', 'python3-pip'])

    # ~570MB: no overall puppet timeout (slow links can exceed 30 min); curl instead aborts a stalled transfer
    # (<10KB/s for 5 min) and retries (-C - resumes a partial /tmp download, also on a later provision).
    # Skip the download if a verified zip is already in /tmp; on an HTTP error (curl 22, e.g. 416 when resuming
    # a complete-but-corrupt file) or a checksum mismatch, delete the zip so the next run starts clean.
    $zip_path = "/tmp/${ghidra_zip}"
    $checksum = "echo '${ghidra_sha256}  ${zip_path}' | sha256sum -c -"
    $download = "curl -fsSL -C - --retry 5 --retry-delay 15 --connect-timeout 60 --speed-limit 10240 --speed-time 300 -o ${zip_path} ${ghidra_url}"
    exec { 'download and unpack ghidra':
      command  => "if ! ${checksum} >/dev/null 2>&1; then ${download}; rc=\$?; if [ \$rc -eq 22 ]; then rm -f ${zip_path}; fi; [ \$rc -eq 0 ] || exit \$rc; ${checksum} || { rm -f ${zip_path}; exit 1; }; fi; unzip -q ${zip_path} -d /opt && rm -f ${zip_path}",
      creates  => $ghidra_dir,
      provider => shell,
      timeout  => 0,
      require  => Package['curl', 'unzip'],
    }

    file { '/opt/ghidra':
      ensure  => link,
      target  => $ghidra_dir,
      require => Exec['download and unpack ghidra'],
    }

    file_line { 'ghidra java home override':
      path    => "${ghidra_dir}/support/launch.properties",
      match   => '^JAVA_HOME_OVERRIDE=',
      line    => 'JAVA_HOME_OVERRIDE=/usr/lib/jvm/java-21-openjdk-amd64',
      require => [Exec['download and unpack ghidra'], Package['openjdk-21-jdk']],
    }

    # Debugger: gdb's python needs the bundled ghidragdb/ghidratrace/protobuf wheels (offline install from the release).
    # --ignore-installed avoids pip trying to remove apt-managed packages (e.g. an older python3-protobuf).
    exec { 'install ghidra gdb debugger python packages':
      command  => "pip3 install --break-system-packages --ignore-installed --no-index -f ${ghidra_dir}/Ghidra/Debug/Debugger-rmi-trace/pypkg/dist -f ${ghidra_dir}/Ghidra/Debug/Debugger-agent-gdb/pypkg/dist ghidragdb",
      unless   => "python3 -c 'import ghidragdb, ghidratrace'",
      provider => shell,
      require  => [Exec['download and unpack ghidra'], Package['python3-pip', 'gdb']],
    }

    file { '/usr/share/applications/ghidra.desktop':
      content => "[Desktop Entry]\nVersion=1.0\nName=Ghidra\nExec=/opt/ghidra/ghidraRun\nTerminal=false\nType=Application\nStartupNotify=true\nCategories=Development;\nIcon=/opt/ghidra/docs/GhidraClass/Advanced/src/ghidraRight.png\n",
      mode    => '0644',
      owner   => 'root',
      group   => 'root',
      require => File['/opt/ghidra'],
    }
  } else {
    if ($operatingsystem == 'Debian') {
      case $operatingsystemrelease {
        /^(12).*/: { # do 12.x bookworm stuff
          ensure_packages(['openjdk-17-jre', 'openjdk-17-jdk'])
        }
        /^(9|10).*/: { # do 9.x stretch stuff
          ensure_packages(['openjdk-11-jre', 'openjdk-11-jdk'])
        }
        /^7.*/: { # do 7.x wheezy stuff
          # Will error -- TODO needs repo
          ensure_packages(['openjdk-11-jre', 'openjdk-11-jdk'])
        }
        default: {
        }
      }
    }

    # Debian: bundled 11.1.2 release (existing labs / lab sheets are written against this version)
    file { '/opt/ghidra':
      ensure => directory,
      recurse => true,
      source => 'puppet:///modules/ghidra/release',
      mode   => '0777',
      owner => 'root',
      group => 'root',
    } ->
    file { '/usr/share/applications/ghidra.desktop':
      source => 'puppet:///modules/ghidra/ghidra.desktop',
      mode   => '0644',
      owner => 'root',
      group => 'root',
    }
  }

  # put `ghidra` on the PATH (ghidraRun resolves its own location via readlink -f, so a symlink works)
  file { '/usr/local/bin/ghidra':
    ensure  => link,
    target  => '/opt/ghidra/ghidraRun',
    require => File['/opt/ghidra'],
  }

}
