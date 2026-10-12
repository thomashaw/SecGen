# Returns a package repository URL: on KVM guests (the Hacktivity Proxmox/oVirt infrastructure)
# via the local package mirror there, otherwise (VirtualBox etc.) the upstream URL unchanged.
#   secgen_functions::mirror_url('https://download.docker.com/linux/debian')
#     KVM:        http://172.33.0.44/download.docker.com/linux/debian
#     VirtualBox: https://download.docker.com/linux/debian
function secgen_functions::mirror_url($upstream_url) {
  if $facts['virtual'] == 'kvm' {
    regsubst($upstream_url, '^https?://', 'http://172.33.0.44/')
  } else {
    $upstream_url
  }
}
