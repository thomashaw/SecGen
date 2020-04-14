require_relative '../helpers/constants.rb'
require_relative '../helpers/json_functions.rb'

require 'digest/md5'
require 'securerandom'
require 'duplicate'
require 'yaml'

class Module
  #Vulnerability attributes hash
  attr_accessor :module_path # vulnerabilities/unix/ftp/vsftp_234_backdoor
  attr_accessor :module_type # vulnerability|service|utility
  attr_accessor :attributes # attributes are hashes that contain arrays of values
  # Each attribute is stored in a hash containing an array of values (because elements such as author can repeat).
  # Module *selectors*, store filters in the attributes hash.
  # XML validity ensures valid and complete information.

  attr_accessor :write_to_module_with_id # the module instance that this module writes to
  attr_accessor :write_to_datastore # the datastore to store the result to
  attr_accessor :write_module_path_to_datastore # the datastore to store the result to
  attr_accessor :write_output_variable # the variable/fact written to
  attr_accessor :output # the result of local processing
  attr_accessor :unique_id # the unique id for this module *instance*
  attr_accessor :received_inputs # any locally calculated inputs fed into this module instance
  attr_accessor :received_datastores # any datastores to be fed into this module instance

  attr_accessor :conflicts
  attr_accessor :requires
  attr_accessor :goals
  attr_accessor :puppet_file
  attr_accessor :puppet_other_path
  attr_accessor :local_calc_file

  attr_accessor :default_inputs_selectors # hash of into => module_selector
  attr_accessor :default_inputs_literals # hash of into => literal values

  # @param [Object] module_type: such as 'vulnerability', 'base', 'service', 'network'
  def initialize(module_type)
    self.module_type = module_type
    self.conflicts = []
    self.requires = []
    self.goals = []
    self.attributes = {}
    self.output = []
    self.write_to_module_with_id = write_output_variable = ''
    self.received_inputs = {}
    self.received_datastores = {} # into_variable => [[variablename] and [access], ]
    self.default_inputs_selectors = {}
    self.default_inputs_literals = {}

    # self.attributes['module_type'] = module_type # add as an attribute for filtering
  end

  def inspect
    "SECGEN_MODULE(type:#{module_type} path:#{module_path} attr:#{attributes.inspect} to:#{write_to_module_with_id}.#{write_output_variable} id:#{unique_id} received_inputs:#{received_inputs} default_inputs_selectors: #{default_inputs_selectors} default_inputs_literals: #{default_inputs_literals})"
  end

  # @return [Object] a string for console output
  def to_s
    (<<-END)
#{module_type}: #{module_path}
      attributes: #{attributes.inspect}
      conflicts: #{conflicts.inspect}
      requires: #{requires.inspect}
      goals: #{goals.inspect}
      puppet file: #{puppet_file}
      puppet path: #{puppet_other_path}
    END
  end

  # @return [Object] a string for Vagrant/Ruby file comments
  def to_s_comment
    out = input = ''
    if received_inputs != {}
      input = "\n    #   received_inputs: #{self.received_inputs}"
    end
    if write_to_module_with_id != ''
      out = "\n    #   writes out ('#{self.output}') to #{self.write_to_module_with_id} -> #{self.write_output_variable}"
    end

    (<<-END)
    # #{module_type}: #{module_path}
    #   id: #{unique_id}
    #   attributes: #{attributes.inspect}
    #   conflicts: #{conflicts.inspect}
    #   goals: #{goals.inspect}
    #   requires: #{requires.inspect}#{input}#{out}
    END
  end

  # @return [Object] the leaf directory (last part of the module path)
  def module_path_end
    match = module_path.match(/.*?([^\/]*)$/i)
    match.captures[0]
  end

  # @return [Object] the module path with _ rather than / for use as a variable name
  def module_path_name
    module_path_name = module_path.clone
    module_path_name.gsub!('/', '_')
  end

  # @return [Object] a list of attributes that can be used to re-select the same modules
  def attributes_for_scenario_output
    attr_flattened = {}

    attributes.each do |key, array|
      unless "#{key}" == 'module_type' || "#{key}" == 'conflict' || "#{key}" == 'default_input' || "#{key}" == 'requires' || "#{key}" == 'goals'
        # creates a valid regexp that can match the original module
        attr_flattened["#{key}"] = Regexp.escape(array.join('~~~')).gsub(/\n\w*/, '.*').gsub(/\\ /, ' ').gsub(/~~~/, '|')
      end
    end

    attr_flattened
  end

  # A one directional test for conflicts
  # Returns whether this module specifies it conflicts with the other_module.
  # Each conflict can have multiple conditions which must all be met for this
  # to be considered a conflict. However, only one conflict needs to be satisfied.
  # @param [Object] other_module to compare with
  # @return [Object] boolean
  def conflicts_with(other_module)
    # for each conflict
    self.conflicts.each do |conflict|
      if other_module.matches_attributes_requirement(conflict)
        return true
      end
    end
    false
  end

  def matches_attributes_requirement(required)
    all_conditions_met = true
    required.keys.each do |require_key|
      key_matched = false

      if self.attributes["#{require_key}"] != nil
        # for each corresponding value in the previously selected module
        self.attributes["#{require_key}"].each do |value|
          # for each value in the required list
          required["#{require_key}"].each do |required_value|
            required_value = prepare_required_value(require_key, required_value)
            if Regexp.new(required_value).match(value)
              key_matched = true
            elsif required_value.include? '?!'
              # Return false immediately if we've got a negative regex match as this module definitely does not match requirements.
              return false
            end
          end
        end
      end
      # any failure to match
      unless key_matched
        return false
      end
    end
    all_conditions_met
  end

  def prepare_required_value(required_key, value)
    if required_key == 'module_path'
      # allow omission of 'modules/' e.g. <module_path>services/platform/module_name</module_path>
      if value.partition('/').first != 'modules'
        value = 'modules/' + value
      end
      # wrap value with ^ and $ to limit start/end of string.
      value = "^#{value}$"
    elsif required_key == 'privilege' || required_key == 'type'
      value = "^#{value}$"
    end
    value
  end

  # Get the system that this module is for, based on the unique_id.
  # If there is more that 1 system we gets the first integer e.g. the 1 in scenariosystem1
  def system_number
    split_string_array = unique_id.split('scenariosystem')

    if split_string_array[1][0] =~ /[[:alpha:]]/
      1 # only 1 system so return 1
    elsif split_string_array[1][0] =~ /[[:digit:]]/
      split_string_array[1][0].to_i # return the system id
    end
  end

  # Get unique rule id for the module based on a rule key/value pair
  def get_unique_rule_id(prefix, system_name, rule_key, rule_value)
    # TODO: This might be too long, see if there is a length limit for rule identities
    prefix = prefix + "_" if prefix and prefix != ''
    "#{prefix}#{system_name}_#{module_path_end}_#{rule_key}_#{rule_value.gsub(/[^\w]/, '_')}"
  end

  def printable_name
    "#{self.attributes['name'].first} (#{self.module_path})"
  end

  # Resolve the string interpolation for received inputs
  # e.g. convert "/home#{accounts[0].username}/#{leaked_files}" into the correct string.
  def resolve_received_inputs
    received_inputs_to_hash
    self.received_inputs.each do |input|
      # Resolve the received inputs which contain #{}
      input[1].each_with_index do |string, c|
        input[1][c] = interp_string(string) if contains_interp(string)
      end
    end
    received_inputs_to_json_str
  end

  # Resolve the string interpolation for goals

  def resolve_goals(hostname)
    new_goals = []
    self.goals.each do |goal|
      new_goal = {}

      # Add hostname to module goals
      new_goal.merge!({'hostname' => hostname}) unless goal.has_key? 'hostname'

      # Interpolate values that require it
      goal.each_key do |key|
        value = goal[key]
        new_goal.merge!(key => (contains_interp(value) ? interp_string(value) : value))
      end
      new_goals << new_goal
    end
    self.goals = new_goals
  end

  def contains_interp(string)
    string.include?('#{') and string.include?('}')
  end

  def received_inputs_to_hash
    self.received_inputs.each do |_, array|
      array.each_with_index do |value, i|
        if JSONFunctions.is_json?(value)
          array[i] = JSON.parse(value)
        end
      end
    end
  end

  def received_inputs_to_json_str
    self.received_inputs.each do |_, array|
      array.each_with_index do |value, i|
        if value.is_a? Hash
          array[i] = value.to_json
        end
      end
    end
  end

  def interp_string(string)
    begin
      # identify the indices of the #{ characters within the string
      start_indices = string.enum_for(:scan, /#\{/).map {Regexp.last_match.begin(0)}
      reference_string = "self.received_inputs"
      start_indices.each_with_index do |index, counter|
        rolling_index = index + 2 # we add 2 for the #{ characters
        if counter > 0
          rolling_index = reference_string.length + index + 2
        end
        string.insert(rolling_index, reference_string)
      end

      string = JSONFunctions.sanitise_eval_string(string)
      # evaluate and parse evaluated string into required data types(e.g. "['a',['b','c']]" into ['a',['b','c']])
      YAML.load(instance_eval("\"#{string}\""))
    rescue NoMethodError, SyntaxError, Psych::Exception => err
      Print.err "#{err}"
      raise 'failed'
    end
  end
end
