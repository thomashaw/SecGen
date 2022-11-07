#!/usr/bin/ruby
require_relative '../../../../../lib/objects/local_string_generator.rb'
require 'faker'

class UKAddressGenerator < StringGenerator
  def initialize
    super
    self.module_name = 'Random UK Address Generator'
  end

  def generate
    # Faker::Config.locale = 'en-GB'
    # TODO: Currently a bug in Faker library with en-gb, use default for now.

    street_name = Faker::Address.street_address
    city = Faker::Address.city
    # county = Faker::Address.county
    postcode = Faker::Address.postcode

    # self.outputs << street_name + ', ' + city + ', ' + county + ', ' + postcode
    self.outputs << street_name + ', ' + city + ', ' + postcode
  end
end

UKAddressGenerator.new.run