#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_encoder.rb'
require 'json'
require 'securerandom'

# Picks the random values for one lab. Every value the student has to work with
# or could share differs between builds.
class AiLabParams < StringEncoder
  attr_accessor :lab

  def initialize
    super
    self.module_name = 'AI lab parameters'
    self.lab = 'model_stealing'
  end

  # A shuffle of 0-9 with no value left in place, so no class keeps its digit.
  def derangement
    loop do
      map = (0..9).to_a.shuffle(random: SecureRandom)
      return map if map.each_with_index.none? { |v, i| v == i }
    end
  end

  def model_stealing
    budget = SecureRandom.random_number(3001..4500)
    {
      'label_map' => derangement,
      'query_budget' => budget,
      'tight_budget' => SecureRandom.random_number(350..500),
      'agreement_threshold' => 0.94,
      'tight_threshold' => 0.87,
      'check_sample' => 1000,
      'pool_size' => 12_000,
      'gradient_endpoint' => false
    }
  end

  def encode_all
    params = { 'lab' => lab, 'seed' => SecureRandom.random_number(1..2_000_000_000) }
    params.merge!(send(lab))
    outputs << params.to_json
  end

  def process_options(opt, arg)
    super
    self.lab = arg if opt == '--lab'
  end

  def get_options_array
    super + [['--lab', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def encoding_print_string
    "lab: #{lab}"
  end
end

AiLabParams.new.run
