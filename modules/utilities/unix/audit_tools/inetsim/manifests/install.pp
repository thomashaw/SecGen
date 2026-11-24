# Installs INetSim from the Debian archive, and dnsmasq for the hosts where
# INetSim's own DNS service cannot run (see files/inetsim-dns-needs-dnsmasq).
class inetsim::install {
  ensure_packages(['inetsim', 'dnsmasq-base'])
}
