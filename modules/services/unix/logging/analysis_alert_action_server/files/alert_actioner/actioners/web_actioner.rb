require 'net/http'
require 'uri'
require_relative 'alert_actioner'

class WebActioner < AlertActioner

  attr_accessor :target_host
  attr_accessor :request_type
  attr_accessor :data

  def initialize(config_filename, alertaction_index, alert_name, target_host, request_type, data)
    super(config_filename, alertaction_index, alert_name)
    self.target_host = target_host
    self.request_type = request_type
    self.data = data
  end

  def perform_action
    # uri = URI.parse("http://www.google.com")
    uri = URI.parse(self.target_host)
    case self.request_type
    when 'GET'
      ENV['http_proxy'] = "http://172.22.0.51:3128"  # TODO: hard-coded temporary fix. Parameterise me!
      response = Net::HTTP.get_response(uri)
    when 'POST'
      ENV['http_proxy'] = "http://172.22.0.51:3128"  # TODO: hard-coded temporary fix. Parameterise me!
      http = Net::HTTP.new(uri.host, uri.port)
      http.use_ssl = true
      http.set_debug_output($stdout)
      request = Net::HTTP::Post.new(uri.request_uri)
      # request.body = URI.encode(self.data)  # commented as we're putting the parameter string directly into the url
      request["Content-Type"] = "application/json"
      request["User-Agent"] = "curl/7.55.1"
      response = http.request(request)
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
    "WebActioner:\n  Target URL: #{self.target_host}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end