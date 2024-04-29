class notepadplusplus::install {
  package { 'notepadplusplus':
    provider => chocolatey,
    ensure => installed,
  }
}
