#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'

class NetworksDnsTlsVpn < HackerbotConfigGenerator

  attr_accessor :IP_addresses
  attr_accessor :dns_txt_flag
  attr_accessor :tls_cert_flag
  attr_accessor :uri
  attr_accessor :vpn_flag
  attr_accessor :vpn_flag_port

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Networks DNS TLS VPN'
    self.title = 'Secure Communications: VPNs, SSL/TLS and DNS Security'
    self.local_dir = File.expand_path('../../', __FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/lab.xml.erb"
    self.html_template_path = "#{self.local_dir}/templates/labsheet.html.erb"

    self.IP_addresses = []
    self.dns_txt_flag = ''
    self.tls_cert_flag = ''
    self.uri = ''
    self.vpn_flag = ''
    self.vpn_flag_port = ''
  end

  def get_options_array
    super + [
      ['--IP_addresses',  GetoptLong::REQUIRED_ARGUMENT],
      ['--dns_txt_flag',  GetoptLong::REQUIRED_ARGUMENT],
      ['--tls_cert_flag', GetoptLong::REQUIRED_ARGUMENT],
      ['--uri',           GetoptLong::REQUIRED_ARGUMENT],
      ['--vpn_flag',      GetoptLong::REQUIRED_ARGUMENT],
      ['--vpn_flag_port', GetoptLong::REQUIRED_ARGUMENT],
    ]
  end

  def process_options(opt, arg)
    super
    case opt
    when '--IP_addresses'
      self.IP_addresses << arg
    when '--dns_txt_flag'
      self.dns_txt_flag << arg
    when '--tls_cert_flag'
      self.tls_cert_flag << arg
    when '--uri'
      self.uri << arg
    when '--vpn_flag'
      self.vpn_flag << arg
    when '--vpn_flag_port'
      self.vpn_flag_port << arg
    end
  end

  def encoding_print_string
    "IP_addresses: #{self.IP_addresses}, dns_txt_flag: #{self.dns_txt_flag}, " \
      "tls_cert_flag: #{self.tls_cert_flag}, uri: #{self.uri}, " \
      "vpn_flag: #{self.vpn_flag}, vpn_flag_port: #{self.vpn_flag_port}"
  end

end

NetworksDnsTlsVpn.new.run