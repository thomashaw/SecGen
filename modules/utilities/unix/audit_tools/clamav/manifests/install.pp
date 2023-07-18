class clamav::install {
  ensure_packages(['clamav', 'clamtk', 'clamav-freshclam'])
}
