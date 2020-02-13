require "json"

# With thanks, from https://gist.github.com/ascendbruce/7070951
class JSONFunctions
  def self.is_json?(value)
    result = JSON.parse(value)
    result.is_a?(Hash)
  rescue JSON::ParserError, TypeError
    false
  end

  # prepare eval string by removing all characters other than #{}[].'_/a-zA-Z0-9
  def self.sanitise_eval_string(string)
    string.gsub(/[^A-Za-z0-9\[\]'\/\_\#\{\}.]/, '')
  end
end