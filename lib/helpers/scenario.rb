class ScenarioHelper

  def self.get_scenario_name(scenario_path)
    scenario_path.split('/').last.split('.').first + '-'
  end

  def self.get_prefix(options, scenario_name)
    options[:prefix] ? (options[:prefix] + '-' + scenario_name) : ('SecGen-' + scenario_name)
  end

  def self.get_hostname(options, scenario_path, system_name)
    "#{get_prefix(options, get_scenario_name(scenario_path))}#{system_name}".tr('_', '-')
  end

end