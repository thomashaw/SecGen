class handy_networking_tools::install{
  ensure_packages([
    'resolvconf',
    'dnsutils',
    'openssl',
    'openvpn',
    'tcpdump',
    'curl',
    'traceroute',
    'whois',
    'net-tools',
  ])
}