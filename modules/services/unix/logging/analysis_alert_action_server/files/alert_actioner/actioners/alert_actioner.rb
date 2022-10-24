require 'fileutils'
require 'erb'
require_relative '../lib/logging'
require_relative '../lib/print'
require_relative '../lib/aa_constants'

class AlertActioner
  include Logging

  attr_accessor :db_id # AlertActioner db_id - database ID for this actioner
  attr_accessor :alertactioner_name # AlertActioner name - unique ID for this actioner
  attr_accessor :alert_name # Alert / Rule name  - ID for elastalert rule that was triggered
  attr_accessor :status # Alert / Rule name  - ID for elastalert rule that was triggered
  attr_accessor :last_actioned # Alert / Rule name  - ID for elastalert rule that was triggered

  def initialize(config_filename, alertaction_index, alert_name)
    self.db_id = -1
    self.alertactioner_name = config_filename[0..-5] + '-' + alertaction_index.to_s + '-' + alertaction_index.to_s # Remove .xml extension
    self.alert_name = alert_name
    self.status = 'todo'
    self.last_actioned = nil
  end

  def perform_action
    # override me
  end

  def action_alert
    Print.info("Running #{self.class}: #{self.alertactioner_name}", logger)
    Print.info("Actioning alert: #{self.alert_name}", logger)
    perform_action
  end

  def template_based_file_write(template, filename)
    template_out = ERB.new(File.read(template), 0, '<>-')

    begin
      File.open(filename, 'wb+') do |file|
        file.write(template_out.result(self.get_binding))
      end
    rescue StandardError => e
      Print.err "Error writing file: #{e.message}"
      Print.err e.backtrace.inspect
    end
  end

  def get_binding
    binding
  end

end