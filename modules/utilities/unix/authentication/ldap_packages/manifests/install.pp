class ldap_packages::install {
  # LDAP Client Utilities
  # Provides command-line tools for interacting with LDAP directories
  ensure_packages(['ldap-utils'])

  # NSS and PAM LDAP Integration
  # Enables system authentication and name service lookups via LDAP
  ensure_packages(['libnss-ldap', 'libpam-ldap'])

  # NSS LDAP Daemon
  # Daemon that performs LDAP queries for NSS and PAM
  ensure_packages(['nslcd'])

  # Name Service Cache Daemon
  # Caches name service lookups to improve performance
  ensure_packages(['nscd'])

  # System Security Services Daemon
  # Provides access to identity and authentication remote resource providers
  ensure_packages(['sssd'])
}
