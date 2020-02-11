require "json"

# With thanks, from https://gist.github.com/ascendbruce/7070951
class JSONFunctions
  def self.is_json?(value)
    result = JSON.parse(value)
    result.is_a?(Hash)
  rescue JSON::ParserError, TypeError
    false
  end
end