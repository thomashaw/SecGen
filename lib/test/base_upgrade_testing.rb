################################################################################
#
#  Script for generating test scenarios for identifying conflicts and
#  version mismatches within a recently upgraded base box.
#
#  1) A scenario file is created for each software module within lib/test/tmp/
#  2) a batch_secgen entry is created for each generated scenario
#  3) batch_secgen spins up each scenario and reports on failures
#
################################################################################

require_relative '../helpers/print.rb'
require_relative '../helpers/constants.rb'
require_relative '../../lib/readers/module_reader.rb'
require_relative '../output/xml_scenario_generator.rb'
require_relative '../objects/system'
require_relative '../objects/module'

def select_base
  bases = ModuleReader.read_bases
  Print.info 'Now listing all available bases: '

  bases.each_with_index do |base, i|
    Print.info "#{i}: #{base.attributes['name'][0]} at #{base.attributes['module_path'][0]}"
  end

  Print.info 'Input the index for the base you want to generate test scenarios for:'
  user_index = gets.chomp.to_i
  selected_base = bases[user_index]
  Print.info "You have selected: #{selected_base.attributes['name'][0]} at #{selected_base.attributes['module_path'][0]}"

  selected_base
end

def get_network_module
  network_module = Module.new('network')
  network_module.attributes['type'] = ['private_network']
  network_module
end

def generate_scenarios(selected_base)
  Dir.mkdir 'tmp' unless Dir.exists? 'tmp'

  # Get installable software modules (vulns, services, utilities)

  vulnerabilities = ModuleReader.read_vulnerabilities
  services = ModuleReader.read_services
  utilities = ModuleReader.read_utilities

  available_software_modules = vulnerabilities + services + utilities


  # If the module conflicts with the base, remove it.
  available_software_modules.delete_if {|module_for_possible_exclusion|
    (module_for_possible_exclusion.conflicts_with(selected_base) ||
        selected_base.conflicts_with(module_for_possible_exclusion))
  }

  available_software_modules.each do |mod|
    ## Create a system_name based on the selected module and the base name
    system_name = "#{selected_base.module_path_end}_#{mod.module_path_end}"

    # Clean up name
    system_name = system_name.gsub(/ /, "_")
    system_name = system_name.gsub(/\//, "")

    module_selections = []
    module_selections << selected_base
    module_selections << mod
    module_selections << get_network_module

    system = System.new(system_name, {}, [])
    system.module_selections = module_selections

    xml_generator = XmlScenarioGenerator.new([system], system_name, Time.new.to_s)
    xml_content = xml_generator.output

    output_filename = "tmp/#{system_name}.xml"
    Print.std "Creating scenario definition file: #{output_filename}"
    begin
      File.open(output_filename, 'w+') do |file|
        file.write(xml_content)
      end
    rescue StandardError => e
      Print.err "Error writing file: #{e.message}"
      abort
    end
  end
end

Print.info 'Script for generating base module upgrade test scenarios'
base = select_base
generate_scenarios(base)