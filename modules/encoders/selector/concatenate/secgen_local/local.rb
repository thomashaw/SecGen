#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_encoder.rb'
class RandomSelectorEncoder < StringEncoder
  attr_accessor :delim

  def initialize
    super
    self.module_name = 'Concatenate'
    self.delim = "\n"
  end

  def encode_all
    selected_string = self.strings_to_encode.join(self.delim)
    self.outputs << selected_string
  end

  def process_options(opt, arg)
    super
    case opt
      # Removes any non-alphabet characters
    when '--delim'
        self.delim = arg;
    end
  end

  def get_options_array
    super + [['--delim', GetoptLong::OPTIONAL_ARGUMENT]]
  end

  # def encoding_print_string
  #   string = "strings_to_encode: #{self.strings_to_encode.to_s}"
  #   string
  # end

end

RandomSelectorEncoder.new.run
