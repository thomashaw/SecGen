require 'net/http'
require 'uri'

class WebActioner
  attr_accessor :url
  attr_accessor :request_type
  attr_accessor :data

  def initialize
    self.url = "https://hacktivity.leedsbeckett.ac.uk/hacktivities/0/challenges/0/vm_sets/0/vms/auto_flag_submit"
    self.request_type = "POST"
    self.data = "vm_name=p-42-472-0-Eeq2-ts-test-grader&flag=flag{test_flag_concat2}"
    # self.data = "vm_name=p-42-472-0-Eeq2-ts_test-grader&flag=flag{test_flag_concat2}"
    # self.data = "vm_name=SecGen-experiment-aaa-auto-grading-server&flag=flag{25b691851687eea87dc7a534f9b7932e}"
  end

  def run
    uri = URI.parse(self.url + "?" + URI.encode(self.data))

    case self.request_type
    when 'GET'
      # ENV['http_proxy'] = "http://172.22.0.51:3128"  # TODO: hard-coded temporary fix. Parameterise me!
      response = Net::HTTP.get_response(uri)
    when 'POST'
      # ENV['http_proxy'] = "http://172.22.0.51:3128"  # TODO: hard-coded temporary fix. Parameterise me!
      http = Net::HTTP.new(uri.host, uri.port)
      http.use_ssl = true
      http.set_debug_output($stdout) #
      request = Net::HTTP::Post.new(uri.request_uri)
      # request.body = URI.encode(self.data)  # commented as we're putting the parameter string directly into the url
      request["Content-Type"] = "application/text"
      request["User-Agent"] = "curl/7.55.1"
      puts request.to_s
      response = http.request(request)
      puts response.to_s
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