class ad_packages::install {
  # Active Directory Integration - Realm Management
  # Manages enrollment and integration with realms (Active Directory domains)
  ensure_packages(['realmd'])

  # System Security Services Daemon
  # Provides access to identity and authentication remote resource providers
  ensure_packages(['sssd', 'sssd-tools'])

  # NSS and PAM SSSD Integration
  # Enables system authentication and name service lookups via SSSD
  ensure_packages(['libnss-sss', 'libpam-sss'])

  # Active Directory CLI
  # Command-line tool for performing actions on an Active Directory domain
  ensure_packages(['adcli'])

  # Samba Common Utilities
  # Common tools for interacting with Windows/Active Directory
  ensure_packages(['samba-common-bin'])

  # OddJob - On-demand D-Bus System
  # Provides on-demand services, including home directory creation
  ensure_packages(['oddjob', 'oddjob-mkhomedir'])
}
