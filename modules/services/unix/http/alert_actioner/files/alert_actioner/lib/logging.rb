module Logging
  require_relative 'aa_constants'
  def logger
    Logging.logger
  end

  def self.logger
    @logger ||= Logger.new(ALERTER_DIRECTORY + 'alert-router.log')
  end
end