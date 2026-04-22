require 'net/http'
require 'uri'
require 'open3'
require_relative 'alert_actioner'
require_relative '../lib/ovirt'

class VirtActioner < AlertActioner

  attr_accessor :vdi_conf # {
                          #   self.virt_type = type,       # [ovirt/other]
                          #   self.ovirtpass = password,
                          #   self.ovirturl = url,
                          #   self.ovirtauthz = authz,
                          #   self.ovirtcluster = cluster,

                          #   self.ovi rtnetwork = network
                          # }

  attr_accessor :command

  def initialize(config_filename, alertaction_index, alert_name, vdi_conf, commands)
    super(config_filename, alertaction_index, alert_name)
    self.vdi_conf = vdi_conf
  end

  def perform_action

    command
  end

  def to_s
    "TODO" #TODO
  end

end