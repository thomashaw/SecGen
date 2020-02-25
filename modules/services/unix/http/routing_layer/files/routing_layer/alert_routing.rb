require_relative 'alerts/alert_factory'

def usage
  Print.std "Usage:
   #{$0} <command> <args>

   COMMANDS:
   alert, a: Read and action an alert  [JSON string]
   load_config, l: Loads the configuration xml file [config.xml]
"
  exit
end


# at least one command
if ARGV.length < 1
  Print.err 'Missing command'
  usage
  exit 1
end

case ARGV[0]
when 'alert', 'a'
  config_loaded_check
  alert = AlertFactory.get_alert(ARGV[1])
  actioner = AlertActioner.get_actioner(alert.type)
  AlertActioner.action(alert)
when 'load_config', 'load', 'l'
  # validate_config(config_string)
  # load_config(config)
else
  Print.err "Command not valid: #{ARGV[0]}"
  usage
  exit 1
end


def config_loaded_check
  # Check if the database has values in, if not error out.
end
