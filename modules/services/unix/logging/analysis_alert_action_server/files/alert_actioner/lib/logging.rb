module Logging
  require 'logger'
  require_relative 'aa_constants'
  def logger
    Logging.logger
  end

  def self.logger
    # TODO: for local testing uncomment me
    # alerter_directory = '/home/thomashaw/git/SecGen/'
    # @logger ||= Logger.new(alerter_directory + 'alert-router.log')
    @logger ||= Logger.new(ALERTER_DIRECTORY + 'alert-router.log')
  end
end