require 'json'
require 'logger'

require_relative 'alerts/alert'
require_relative 'lib/logging'
require_relative 'lib/aa_constants'
require_relative 'lib/print'
require_relative 'lib/alertaction_reader'

class AlertRouter
  attr_accessor :alert
  attr_accessor :alert_actioners
  attr_accessor :input

  include Logging

  def initialize
    self.alert_actioners = []
    Print.debug "AlertRouter started...", logger
    self.input = ARGF.read
    # self.input = 'example-rule:||:[{"_type": "doc", "_index": "auditbeat-2020.03.10", "process": {"exe": "/bin/cat", "name": "cat", "title": "cat testfile", "pid": "1459", "ppid": "1348", "cwd": "/home/vagrant"}, "num_hits": 2, "@timestamp": "2020-03-10T16:57:29.080Z", "tags": ["home", "beats_input_raw_event"], "auditd": {"paths": [{"nametype": "NORMAL", "ouid": "1000", "ogid": "1000", "rdev": "00:00", "dev": "08:01", "item": "0", "mode": "0100644", "inode": "1442062", "name": "testfile"}], "sequence": 2447, "summary": {"how": "/bin/cat", "object": {"type": "file", "primary": "testfile"}, "actor": {"primary": "vagrant", "secondary": "vagrant"}}, "session": "3", "result": "success", "data": {"tty": "pts1", "syscall": "open", "a1": "0", "a0": "7ffe67dd1418", "a3": "69f", "a2": "fffffffffffe0400", "exit": "3", "arch": "x86_64"}}, "beat": {"hostname": "shaw54-AGT-17-auto-grading-tracer-client-1", "name": "shaw54-AGT-17-auto-grading-tracer-client-1", "version": "6.8.7"}, "host": {"name": "shaw54-AGT-17-auto-grading-tracer-client-1"}, "user": {"fsuid": "1000", "auid": "1000", "uid": "1000", "name_map": {"fsuid": "vagrant", "auid": "vagrant", "uid": "vagrant", "suid": "vagrant", "fsgid": "vagrant", "egid": "vagrant", "euid": "vagrant", "gid": "vagrant", "sgid": "vagrant"}, "suid": "1000", "fsgid": "1000", "egid": "1000", "euid": "1000", "gid": "1000", "sgid": "1000"}, "file": {"group": "vagrant", "uid": "1000", "owner": "vagrant", "gid": "1000", "mode": "0644", "device": "00:00", "path": "testfile", "inode": "1442062"}, "combined_path": "/home/vagrant/testfile", "num_matches": 1, "_id": "mHthxXABcON1JJkPPtdf", "@version": "1", "event": {"action": "opened-file", "category": "audit-rule", "type": "syscall", "module": "auditd"}}]'
    Print.info "Alert received: #{self.input}", logger
    load_configs
  end

  def parameter_check
    unless self.input
      Print.err 'ERROR: No input received.', logger
      exit(1)
    end
    unless self.input.include? ':||:'
      Print.err 'ERROR: Does not include delimiter, :||:, between alert-name and JSON. Was this run via Elastalert?', logger
      exit(1)
    end
  end

  def read_alert
    begin
      split_input = self.input.split(':||:')
      raise StandardError if split_input.length != 2
      rule_name = split_input[0]
      alert_body = JSON.parse(split_input[1])
      Print.info("Reading alert for rule: #{rule_name}", logger)
      self.alert = Alert.new(rule_name, alert_body)
    rescue JSON::ParserError, StandardError => e
      Print.info(e.to_s, logger)
      exit(1)
    end
  end

  def load_configs
    Print.info("Reading config files from #{CONFIG_DIRECTORY}", logger)
    conf_filenames = Dir["#{CONFIG_DIRECTORY}*.xml"]
    conf_filenames.each do |conf_path|
      Print.info("Loading config: #{conf_path}", logger)
      self.alert_actioners = AlertActionReader.get_alertactioners(conf_path)
    end
  end

  def rule_comparison
    # Does the alert match our rules? if so: action the alert.
    self.alert_actioners.each do |alert_actioner|
      if alert_actioner.alert_name == self.alert.rule_name
        alert_actioner.action_alert
      end
    end
  end

  def run
    parameter_check
    read_alert
    rule_comparison
  end
end

AlertRouter.new.run