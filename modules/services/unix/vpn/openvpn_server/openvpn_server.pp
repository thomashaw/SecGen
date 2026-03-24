class openvpn_server {
  include openvpn_server::install
  include openvpn_server::config
  include openvpn_server::service
}