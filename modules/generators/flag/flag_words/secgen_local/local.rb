#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require_relative '../../../../../lib/helpers/blacklist.rb'
class WordFlagGenerator < StringGenerator
  attr_accessor :number_of_words

  def initialize
    super
    self.module_name = 'Random Word Based Flag Generator'
    # Default to 4
    self.number_of_words = 4
  end

  def generate
    file = File.readlines("#{WORDLISTS_DIR}/wordlist")
    flag_string = ''
    blacklist = Blacklist.new

    self.number_of_words.times do |i|
      flag_word = ''
      until flag_word != ''
        selected_word = file.sample.chomp
        unless blacklist.is_blacklisted? selected_word
          flag_word = selected_word
          flag_string += flag_word
        end
      end
      flag_string.gsub!(/[^0-9a-z ]/i, '')  # strip special characters from the word string. removes umlauts/accents etc.
      self.outputs << "flag{#{flag_string.downcase}}"
      flag_string = ''
    end
  end

  def process_options(opt, arg)
    super

    case opt
      when '--number_of_words'
        if arg.to_i == 0
          self.number_of_words = 1
        else
          self.number_of_words = arg.to_i
        end
      end
  end

  def get_options_array
    super + [['--number_of_words', GetoptLong::OPTIONAL_ARGUMENT]]
  end
end

WordFlagGenerator.new.run
