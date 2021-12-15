require 'pathname'
ALERTER_DIRECTORY = (Pathname.new(File.expand_path(File.dirname(__FILE__))).parent.to_s) + "/"
CONFIG_DIRECTORY = ALERTER_DIRECTORY + 'config/'
LIB_DIRECTORY = ALERTER_DIRECTORY + 'lib/'
AA_CONFIG_SCHEMA = LIB_DIRECTORY + 'alertactioner_config_schema.xsd'
TEMPLATES_DIRECTORY = ALERTER_DIRECTORY + 'templates/'
COMMANDS_DIRECTORY = CONFIG_DIRECTORY + 'commands/'