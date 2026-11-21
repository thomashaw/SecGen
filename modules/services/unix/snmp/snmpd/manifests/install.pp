# Installs the Net-SNMP agent and the snmp client tools.
class snmpd::install {
  ensure_packages(['snmpd', 'snmp'])
}
