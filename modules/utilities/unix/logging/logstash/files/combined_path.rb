# the value of `params` is the value of the hash passed to `script_params`
# in the logstash configuration
def register(params)
  # do nothing, no logstash params
end

# the filter method receives an event and must return a list of events.
# Dropping an event means not including it in the return array,
# while creating new ones only requires you to add a new instance of
# LogStash::Event to the returned array
def filter(event)
  event.set("combined_path", "test")
  [event]
end