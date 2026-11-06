class bind9_zone_transfer::install {
  # Reuse the bind9 service module to install, configure and run the DNS server.
  # bind9::install/config/service read their inputs from $::base64_inputs_file,
  # which the Vagrantfile sets per Puppet provision block — so inside this
  # module's provision block they read this module's received_inputs. The
  # vulnerable values come from this module's default_inputs: allow_transfer
  # 'any' (an unrestricted zone transfer policy) and a zone populated with
  # internal hosts and a TXT flag. The required bind9 service module runs first
  # with its safe defaults; this block re-applies the config with the vulnerable
  # values, so the final state is the misconfiguration we want to teach.
  include bind9
}
