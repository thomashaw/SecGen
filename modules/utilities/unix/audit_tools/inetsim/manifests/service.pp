# Starts INetSim now and at boot (start_service true, the default), restarting it when its
# config changes; or leaves it installed, configured, stopped and disabled (start_service
# false), for students to start themselves with 'sudo systemctl start inetsim'.
# Either way inetsim-dns.service follows INetSim (see config.pp).
# provider => systemd: on Kali, Puppet picks the init provider, which cannot
# enable a service, and Kali's update-rc.d leaves INetSim disabled at install.
class inetsim::service {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $start_service = str2bool($secgen_parameters['start_service'][0])

  service { 'inetsim':
    ensure    => $start_service,
    enable    => $start_service,
    provider  => systemd,
    subscribe => Class['inetsim::config'],
  }
}
