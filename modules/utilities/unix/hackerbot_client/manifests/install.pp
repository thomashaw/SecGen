class hackerbot_client::install {
  # openssh-server should already be present on most bases, but be explicit
  ensure_packages(['openssh-server'])
}
