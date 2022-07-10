require 'net/http'
require 'uri'
require_relative 'alert_actioner'

class WebActioner < AlertActioner

  attr_accessor :hacktivity_url
  attr_accessor :request_type
  attr_accessor :data


  def initialize(config_filename, alertaction_index, alert_name, hacktivity_url, request_type, data)
    super(config_filename, alertaction_index, alert_name)
    self.hacktivity_url = hacktivity_url
    self.request_type = request_type
    self.data = data
  end

  def perform_action
    uri = URI.parse("http://www.google.com")
    # uri = URI.parse(self.hacktivity_url)
    case self.request_type
    when 'GET'
      ENV['http_proxy'] = "http://172.22.0.51:3128"  # TODO: hard-coded temporary fix. Parameterise me!
      response = Net::HTTP.get_response(uri)
    when 'POST'
      request = Net::HTTP::Post.new(uri.request_uri, 'Content-Type' => 'application/json')
      request.body = self.data
      response = Net::HTTP.start(uri.hostname, uri.port) do |http|
        http.request(request)
      end
    when 'PUT'
      # TODO: later
      response = ''
    when 'DELETE'
      # TODO: later
      response = ''
    else
      response = Net::HTTP.get_response(uri)
    end
    Print.info response.body.to_s, logger
    Print.info "Web Action complete #{alertactioner_name}", logger
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "WebActioner:\n  Hacktivity URL: #{self.hacktivity_url}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end