class dead_image::install {

  # The image (~920 MiB) is too big and binary to include in the git repo, so it is fetched at
  # provision time. Two mirrors, tried in order: the first is on port 80 (reachable from more
  # networks than the :65001 mirror, and cached by the university proxy).
  $urls = [
    'http://z.cliffe.schreuders.org/files/6543367533',
    'http://hacktivity.aet.leedsbeckett.ac.uk:65001/files',
  ]
  $evidence_dir = '/root/evidence'

  ensure_packages(['curl'])

  file { $evidence_dir:
    ensure => 'directory',
  }

  # Puppet's own http file source buffers badly for a file this size and gives up on a slow link,
  # so use curl: retries, resumes a partial download (-C -), and only moves the file into place
  # once the transfer has completed. Falls back to the second mirror if the first fails.
  ['hda1.img', 'md5s'].each |$name| {
    $dest = "${evidence_dir}/${name}"
    $cmds = $urls.map |$url| {
      "curl -fsSL --retry 5 --retry-delay 10 --connect-timeout 30 -C - -o '${dest}.part' '${url}/${name}'"
    }
    exec { "fetch_dead_image_${name}":
      command  => "(${join($cmds, ' || ')}) && mv -f '${dest}.part' '${dest}'",
      provider => 'shell',
      path     => ['/usr/local/sbin', '/usr/local/bin', '/usr/sbin', '/usr/bin', '/sbin', '/bin'],
      creates  => $dest,
      timeout  => 3600,
      require  => [File[$evidence_dir], Package['curl']],
    }
  }

}
