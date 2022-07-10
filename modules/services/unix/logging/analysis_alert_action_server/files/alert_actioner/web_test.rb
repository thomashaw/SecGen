require 'net/http'
require 'uri'

class WebActioner
  attr_accessor :url
  attr_accessor :request_type
  attr_accessor :data

  def initialize
    self.url = "http://www.google.com"
    self.request_type = "GET"
    self.data = nil
  end

  def run
    uri = URI.parse(self.url)

    case self.request_type
    when 'GET'
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
    puts response.body.to_s
    puts
    puts
    puts "Web Action complete"
  end

  # TODO: Override me in superclass to print actioner type + all parameters??
  def to_s
    "WebActioner:\n  URL: #{self.url}\n  Request Type: #{self.request_type}\n  Data: #{self.data.to_s}"
  end

end

WebActioner.new.run