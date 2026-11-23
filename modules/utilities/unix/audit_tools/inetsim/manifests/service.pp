# Starts INetSim now and at boot, restarting it when its config changes.
# provider => systemd: on Kali, Puppet picks the init provider, which cannot
# enable a service, and Kali's update-rc.d leaves INetSim disabled at install.
class inetsim::service {
  service { 'inetsim':
    ensure    => running,
    enable    => true,
    provider  => systemd,
    subscribe => Class['inetsim::config'],
  }
}
