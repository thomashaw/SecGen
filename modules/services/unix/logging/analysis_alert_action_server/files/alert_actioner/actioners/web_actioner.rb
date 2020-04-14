require 'net/http'
require 'uri'
require_relative 'alert_actioner'

class WebActioner < AlertActioner

  attr_accessor :target
  attr_accessor :request_type
  attr_accessor :data


  def initialize(config_filename, alertaction_index, alert_name, target, request_type, data)
    super(config_filename, alertaction_index, alert_name)
    self.target = target
    self.request_type = request_type
    self.data = data
  end

  def perform_action
    uri = URI.parse(self.target)

    case self.request_type
    when 'GET'

      response = Net::HTTP.get_response(uri)
    when 'POST'
      request = Net::HTTP::Post.new(uri.request_uri, 'Content-Type' => 'application/json')
      request.body = self.data.to_json
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
    Print.info response.to_s
    Print.info "Web Action complete #{alertactioner_name}"
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "WebActioner:\n  Target: #{self.target}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end