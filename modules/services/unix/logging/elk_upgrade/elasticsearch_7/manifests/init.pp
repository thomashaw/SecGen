# Top-level Elasticsearch class which may manage installation of the
# Elasticsearch package, package repository, and other
# global options and parameters.
#
# @summary Manages the installation of Elasticsearch and related options.
#
# @example install Elasticsearch
#   class { 'elasticsearch': }
#
# @example removal and decommissioning
#   class { 'elasticsearch':
#     ensure => 'absent',
#   }
#
# @example install everything but disable service(s) afterwards
#   class { 'elasticsearch':
#     status => 'disabled',
#   }
#
# @param ensure
#   Controls if the managed resources shall be `present` or `absent`.
#   If set to `absent`, the managed software packages will be uninstalled, and
#   any traces of the packages will be purged as well as possible, possibly
#   including existing configuration files.
#   System modifications (if any) will be reverted as well as possible (e.g.
#   removal of created users, services, changed log settings, and so on).
#   This is a destructive parameter and should be used with care.
#
# @param api_basic_auth_password
#   Defines the default REST basic auth password for API authentication.
#
# @param api_basic_auth_username
#   Defines the default REST basic auth username for API authentication.
#
# @param api_ca_file
#   Path to a CA file which will be used to validate server certs when
#   communicating with the Elasticsearch API over HTTPS.
#
# @param api_ca_path
#   Path to a directory with CA files which will be used to validate server
#   certs when communicating with the Elasticsearch API over HTTPS.
#
# @param api_host
#   Default host to use when accessing Elasticsearch APIs.
#
# @param api_port
#   Default port to use when accessing Elasticsearch APIs.
#
# @param api_protocol
#   Default protocol to use when accessing Elasticsearch APIs.
#
# @param api_timeout
#   Default timeout (in seconds) to use when accessing Elasticsearch APIs.
#
# @param autoupgrade
#   If set to `true`, any managed package will be upgraded on each Puppet run
#   when the package provider is able to find a newer version than the present
#   one. The exact behavior is provider dependent (see
#   {package, "upgradeable"}[http://j.mp/xbxmNP] in the Puppet documentation).
#
# @param config
#   Elasticsearch configuration hash.
#
# @param configdir
#   Directory containing the elasticsearch configuration.
#   Use this setting if your packages deviate from the norm (`/etc/elasticsearch`)
#
# @param daily_rolling_date_pattern
#   File pattern for the file appender log when file_rolling_type is 'dailyRollingFile'.
#
# @param datadir
#   Allows you to set the data directory of Elasticsearch.
#
# @param datadir_instance_directories
#   Control whether individual directories for instances will be created within
#   each instance's data directory.
#
# @param default_logging_level
#   Default logging level for Elasticsearch.
#
# @param defaults_location
#   Absolute path to directory containing init defaults file.
#
# @param download_tool
#   Command-line invocation with which to retrieve an optional package_url.
#
# @param download_tool_insecure
#   Command-line invocation with which to retrieve an optional package_url when
#   certificate verification should be ignored.
#
# @param download_tool_verify_certificates
#   Whether or not to verify SSL/TLS certificates when retrieving package files
#   using a download tool instead of a package management provider.
#
# @param elasticsearch_group
#   The group Elasticsearch should run as. This also sets file group
#   permissions.
#
# @param elasticsearch_user
#   The user Elasticsearch should run as. This also sets file ownership.
#
# @param file_rolling_type
#   Configuration for the file appender rotation. It can be 'dailyRollingFile',
#   'rollingFile' or 'file'. The first rotates by name, the second one by size
#   or third don't rotate automatically.
#
# @param homedir
#   Directory where the elasticsearch installation's files are kept (plugins, etc.)
#
# @param indices
#   Define indices via a hash. This is mainly used with Hiera's auto binding.
#
# @param init_defaults
#   Defaults file content in hash representation.
#
# @param init_defaults_file
#   Defaults file as puppet resource.
#
# @param init_template
#   Service file as a template.
#
# @param instances
#   Define instances via a hash. This is mainly used with Hiera's auto binding.
#
# @param jvm_options
#   Array of options to set in jvm_options.
#
# @param license
#   Optional Elasticsearch license in hash or string form.
#
# @param logdir
#   Directory that will be used for Elasticsearch logging.
#
# @param logging_config
#   Representation of information to be included in the logging.yml file.
#
# @param logging_file
#   Instead of a hash, you may supply a `puppet://` file source for the
#   logging.yml file.
#
# @param logging_template
#   Use a custom logging template - just supply the relative path, i.e.
#   `$module/elasticsearch/logging.yml.erb`
#
# @param manage_repo
#   Enable repo management by enabling official Elastic repositories.
#
# @param oss
#   Whether to use the purely open source Elasticsearch package distribution.
#
# @param package_dir
#   Directory where packages are downloaded to.
#
# @param package_dl_timeout
#   For http, https, and ftp downloads, you may set how long the exec resource
#   may take.
#
# @param package_name
#   Name Of the package to install.
#
# @param package_provider
#   Method to install the packages, currently only `package` is supported.
#
# @param package_url
#   URL of the package to download.
#   This can be an http, https, or ftp resource for remote packages, or a
#   `puppet://` resource or `file:/` for local packages
#
# @param pid_dir
#   Directory where the elasticsearch process should write out its PID.
#
# @param pipelines
#   Define pipelines via a hash. This is mainly used with Hiera's auto binding.
#
# @param plugindir
#   Directory containing elasticsearch plugins.
#   Use this setting if your packages deviate from the norm (/usr/share/elasticsearch/plugins)
#
# @param plugins
#   Define plugins via a hash. This is mainly used with Hiera's auto binding.
#
# @param proxy_url
#   For http and https downloads, you may set a proxy server to use. By default,
#   no proxy is used.
#   Format: `proto://[user:pass@]server[:port]/`
#
# @param purge_configdir
#   Purge the config directory of any unmanaged files.
#
# @param purge_package_dir
#   Purge package directory on removal
#
# @param purge_secrets
#   Whether or not keys present in the keystore will be removed if they are not
#   present in the specified secrets hash.
#
# @param repo_stage
#   Use stdlib stage setup for managing the repo instead of relationship
#   ordering.
#
# @param restart_on_change
#   Determines if the application should be automatically restarted
#   whenever the configuration, package, or plugins change. Enabling this
#   setting will cause Elasticsearch to restart whenever there is cause to
#   re-read configuration files, load new plugins, or start the service using an
#   updated/changed executable. This may be undesireable in highly available
#   environments. If all other restart_* parameters are left unset, the value of
#   `restart_on_change` is used for all other restart_*_change defaults.
#
# @param restart_config_change
#   Determines if the application should be automatically restarted
#   whenever the configuration changes. This includes the Elasticsearch
#   configuration file, any service files, and defaults files.
#   Disabling automatic restarts on config changes may be desired in an
#   environment where you need to ensure restarts occur in a controlled/rolling
#   manner rather than during a Puppet run.
#
# @param restart_package_change
#   Determines if the application should be automatically restarted
#   whenever the package (or package version) for Elasticsearch changes.
#   Disabling automatic restarts on package changes may be desired in an
#   environment where you need to ensure restarts occur in a controlled/rolling
#   manner rather than during a Puppet run.
#
# @param restart_plugin_change
#   Determines if the application should be automatically restarted whenever
#   plugins are installed or removed.
#   Disabling automatic restarts on plugin changes may be desired in an
#   environment where you need to ensure restarts occur in a controlled/rolling
#   manner rather than during a Puppet run.
#
# @param roles
#   Define roles via a hash. This is mainly used with Hiera's auto binding.
#
# @param rolling_file_max_backup_index
#   Max number of logs to store whern file_rolling_type is 'rollingFile'
#
# @param rolling_file_max_file_size
#   Max log file size when file_rolling_type is 'rollingFile'
#
# @param scripts
#   Define scripts via a hash. This is mainly used with Hiera's auto binding.
#
# @param secrets
#   Optional default configuration hash of key/value pairs to store in the
#   Elasticsearch keystore file. If unset, the keystore is left unmanaged.
#
# @param security_logging_content
#   File content for shield/x-pack logging configuration file (will be placed
#   into logging.yml or log4j2.properties file as appropriate).
#
# @param security_logging_source
#   File source for shield/x-pack logging configuration file (will be placed
#   into logging.yml or log4j2.properties file as appropriate).
#
# @param security_plugin
#   Which security plugin will be used to manage users, roles, and
#   certificates.
#
# @param service_provider
#   The service resource type provider to use when managing elasticsearch instances.
#
# @param snapshot_repositories
#   Define snapshot repositories via a hash. This is mainly used with Hiera's auto binding.
#
# @param status
#   To define the status of the service. If set to `enabled`, the service will
#   be run and will be started at boot time. If set to `disabled`, the service
#   is stopped and will not be started at boot time. If set to `running`, the
#   service will be run but will not be started at boot time. You may use this
#   to start a service on the first Puppet run instead of the system startup.
#   If set to `unmanaged`, the service will not be started at boot time and Puppet
#   does not care whether the service is running or not. For example, this may
#   be useful if a cluster management software is used to decide when to start
#   the service plus assuring it is running on the desired node.
#
# @param system_key
#   Source for the Shield/x-pack system key. Valid values are any that are
#   supported for the file resource `source` parameter.
#
# @param systemd_service_path
#   Path to the directory in which to install systemd service units.
#
# @param templates
#   Define templates via a hash. This is mainly used with Hiera's auto binding.
#
# @param users
#   Define templates via a hash. This is mainly used with Hiera's auto binding.
#
# @param validate_tls
#   Enable TLS/SSL validation on API calls.
#
# @param version
#   To set the specific version you want to install.
#
# @author Richard Pijnenburg <richard.pijnenburg@elasticsearch.com>
# @author Tyler Langlois <tyler.langlois@elastic.co>
#
class elasticsearch (
  Enum['absent', 'present']                       $ensure,
  String                                          $api_host,
  Integer[0, 65535]                               $api_port,
) {



}
