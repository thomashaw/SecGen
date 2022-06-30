require 'json'
require 'logger'
require 'pg'
require 'getoptlong'

require_relative 'alerts/alert'
require_relative 'lib/logging'
require_relative 'lib/aa_constants'
require_relative 'lib/print'
require_relative 'lib/alertaction_reader'

## Globals
@alert = nil
@alert_actioners = []
@input = nil
@db_conn = nil

include Logging

# def add
# self.input = ARGF.read
# self.input = 'example-rule:||:[{"_type": "doc", "_index": "auditbeat-2020.03.10", "process": {"exe": "/bin/cat", "name": "cat", "title": "cat testfile", "pid": "1459", "ppid": "1348", "cwd": "/home/vagrant"}, "num_hits": 2, "@timestamp": "2020-03-10T16:57:29.080Z", "tags": ["home", "beats_input_raw_event"], "auditd": {"paths": [{"nametype": "NORMAL", "ouid": "1000", "ogid": "1000", "rdev": "00:00", "dev": "08:01", "item": "0", "mode": "0100644", "inode": "1442062", "name": "testfile"}], "sequence": 2447, "summary": {"how": "/bin/cat", "object": {"type": "file", "primary": "testfile"}, "actor": {"primary": "vagrant", "secondary": "vagrant"}}, "session": "3", "result": "success", "data": {"tty": "pts1", "syscall": "open", "a1": "0", "a0": "7ffe67dd1418", "a3": "69f", "a2": "fffffffffffe0400", "exit": "3", "arch": "x86_64"}}, "beat": {"hostname": "shaw54-AGT-17-auto-grading-tracer-client-1", "name": "shaw54-AGT-17-auto-grading-tracer-client-1", "version": "6.8.7"}, "host": {"name": "shaw54-AGT-17-auto-grading-tracer-client-1"}, "user": {"fsuid": "1000", "auid": "1000", "uid": "1000", "name_map": {"fsuid": "vagrant", "auid": "vagrant", "uid": "vagrant", "suid": "vagrant", "fsgid": "vagrant", "egid": "vagrant", "euid": "vagrant", "gid": "vagrant", "sgid": "vagrant"}, "suid": "1000", "fsgid": "1000", "egid": "1000", "euid": "1000", "gid": "1000", "sgid": "1000"}, "file": {"group": "vagrant", "uid": "1000", "owner": "vagrant", "gid": "1000", "mode": "0644", "device": "00:00", "path": "testfile", "inode": "1442062"}, "combined_path": "/home/vagrant/testfile", "num_matches": 1, "_id": "mHthxXABcON1JJkPPtdf", "@version": "1", "event": {"action": "opened-file", "category": "audit-rule", "type": "syscall", "module": "auditd"}}]'
# Print.info "Alert received: #{self.input}", logger
# end

def run_alert_actions(db_conn, alert_name)
  # Does the alert match our rules? if so: action the alert.
  @alert_actioners.each do |alert_actioner|
    if alert_actioner.alert_name == alert_name
      if alert_actioner.last_actioned == nil
        # TODO: uncomment before pushing for testing
        alert_actioner.action_alert
        alert_actioner.status = 'actioned'
      else
        Print.info("Alert already actioned, ignoring..:" + alert_actioner.alert_name, logger)
      end
    end
  end
  update_actioned_alerts(db_conn, alert_name)
end

def update_actioned_alerts(db_conn, alert_name)
  db_conn.exec_prepared(@update_statement, [alert_name])
end

def load_configs
  Print.info("Reading config files from #{CONFIG_DIRECTORY}", logger)
  conf_filenames = Dir["#{CONFIG_DIRECTORY}*.xml"]
  conf_filenames.each do |conf_path|
    Print.info("Loading config: #{conf_path}", logger)
    @alert_actioners = AlertActionReader.get_alertactioners(conf_path)
  end
end

########################################
######## Database interactions  ########
########################################

def populate_db(db_conn)
  results = select_all(db_conn)
  if results.values.size > 0 # TODO: Test this with an empty DB
    # pull data out of the db on status and last_actioned timestamp
    results.each do |result|
      if result['status'] != 'todo'
        @alert_actioners.each do |actioner|
          if actioner.alert_name == result['alert_name']
            actioner.status = result['status']
            actioner.last_actioned = result['last_actioned']
          end
        end
      end
    end
  else
    @alert_actioners.each_with_index do |actioner, count|
      statement = "insert_row_#{count}"
      db_conn.prepare(statement, 'insert into alert_events (alert_name, status, last_actioned) values ($1, $2, $3) returning id')
      result = db_conn.exec_prepared(statement, [actioner.alert_name, actioner.status, actioner.last_actioned])
      Print.info("Successfully added AlertAction to DB. \n\tID: " + result.first['id'] + "\n\talert_name: " + actioner.alert_name, logger)
    end
  end
end

def select_all(db_conn)
  db_conn.exec_params('SELECT * FROM alert_events;')
end

def insert_row(prepared_statements, statement_id, secgen_args)
  statement = "insert_row_#{statement_id}"
  # Add --shutdown and --no-tests and strip trailing whitespace
  Print.info "Adding to action_events: '#{statement}' '#{secgen_args}' 'todo'"
  unless prepared_statements.include? statement
    @db_conn.prepare(statement, 'insert into alert_events (alert_name, status, last_actioned) values ($1, $2, $3) returning id')
    prepared_statements << statement
  end
  result = @db_conn.exec_prepared(statement, [secgen_args, 'todo'])
  Print.info "id: #{result.first['id']}"
end



def update_reset_objects(db_conn)
  # Detect the reset

  # grab the resets from the db
  results = db_conn.exec_params("SELECT * FROM alert_events WHERE status = 'alert_received' and last_actioned IS NULL;")

  # for each result we want to update the object in memory to reflect the db object, as it is changed by other instances of the script
  # TODO: refactor me -- this is late night 'just get it working', garbage PoC. Should use ActiveRecord really and update the objects when the db changes... Not sure if AR works for external applications changing the DB changes or not but should investigate
  results.each do |result|
    @alert_actioners.each do |actioner|
      if actioner.alert_name == result['alert_name']
        actioner.status = result['status']
        actioner.last_actioned = result['last_actioned']
      end
    end
  end
end

#############################
##### Command Functions #####
#############################
##         start           ##
##    raise_alert_event    ##
##          list           ##
##          reset          ##
#############################

def start
  Print.debug "AlertRouter started.", logger
  db_conn = PG::Connection.open(:dbname => 'alert_events')
  Print.debug "Database connection successfully established", logger
  load_configs
  populate_db(db_conn)

  # Pull actual prepare out here, maybe prepare them all before we even enter the loop + just store the keys in a global
  @update_statement = "update_actioned_alerts"
  db_conn.prepare(@update_statement, "UPDATE alert_events SET status = 'actioned', last_actioned = CURRENT_TIMESTAMP WHERE alert_name = $1;")

  while true
    update_reset_objects(db_conn)

    # Check if any alerts need actioning
    begin
      results = db_conn.exec_params("SELECT * FROM alert_events WHERE status = 'alert_received' and last_actioned IS NULL;")
    rescue Exception => e
      Print.err "EXCEPTION RAISED!!", logger
      Print.err e.to_s, logger
    end

    if results.cmd_status != "SELECT 0"
      # New alert to action!
      results.each do |result|
        Print.info("Event with 'alert_received' found in database: #{result['alert_name']}")
        run_alert_actions(db_conn, result['alert_name'])
      end
    end
  end
end


def raise_alert_event(raise_alert_event_opts)
  Print.info("!!! Raising alert event !!!", logger)
  alert_name = raise_alert_event_opts[:raised_alert]
  Print.info("    alert_name: " + alert_name, logger)

  db_conn = PG::Connection.open(:dbname => 'alert_events')
  statement = "update_row_raise_#{alert_name}"
  db_conn.prepare(statement, "update alert_events set status = 'alert_received' where alert_name = $1")
  result = db_conn.exec_prepared(statement, [alert_name])
  if result.cmd_status != "UPDATE 0"
    Print.info "Successfully raised.", logger
  else
    Print.err "Error raising alert: #{alert_name}", logger
    exit(1)
  end
end

def list
  Print.info("Listing current alert_events", logger)
  db_conn = PG::Connection.open(:dbname => 'alert_events')
  results = db_conn.exec_params("SELECT * FROM alert_events;")
  results.each do |result|
    Print.info(result.to_s, logger)
  end
end

def reset(reset_opts)
  Print.info("Resetting status and last_actioned", logger)
  db_conn = PG::Connection.open(:dbname => 'alert_events')
  statement = "update_reset"
  db_conn.prepare(statement, "update alert_events set status = 'todo', last_actioned = NULL where status != 'todo' or last_actioned IS NOT NULL")
  result = db_conn.exec_prepared(statement)
  if result.cmd_status != "UPDATE 0"
    num_reset = result.cmd_status[-1]
    Print.info "Succesfully reset #{num_reset} rows."
  else
    Print.err "No rows to reset."
  end
end


###### Options + Parsing ######

def misc_opts
  [['--help', '-h', GetoptLong::NO_ARGUMENT]]
end


def get_raise_alert_event_opts
  raise_alert_event_options = misc_opts + [['--alert-name', '--alert_name', GetoptLong::REQUIRED_ARGUMENT]]
  parse_opts(GetoptLong.new(*raise_alert_event_options))
end

def get_reset_opts
  reset_options = misc_opts + [['--all', GetoptLong::NO_ARGUMENT]]

  options = parse_opts(GetoptLong.new(*reset_options))
  if !options[:all] # and !options[:running] and !options[:failed]
    Print.err 'Error: The reset command requires an argument.'
    usage
  else
    options
  end
end

def parse_opts(opts)
  options = {:instances => '', :max_threads => 3, :id => nil, :all => false}
  opts.each do |opt, arg|
    Print.info(('Parsing option: ' + opt), logger)
    case opt
    when '--alert_name', '--alert-name'
      options[:raised_alert] = arg
    when '--all'
      options[:all] = true
    else
      Print.err 'Invalid argument'
      exit(false)
    end
  end
  options
end

begin
Print.info("Argument vector: " + ARGV.to_s, logger)
case ARGV[0]
when 'start'
  start
when 'raise'
  raise_alert_event(get_raise_alert_event_opts)
when 'list'
  list
when 'reset'
  reset(get_reset_opts)
else
  Print.err "Unknown command", logger
  usage
end
rescue Exception => e
  Print.err e.to_s, logger
end