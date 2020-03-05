def remove_duplicates(path)   # could this be a problem with things like remote file systems? we'll see...
  simple_path = path
  if path.include?('//')
    second_segment = path.split('//')[1]
    simple_path = "/" + second_segment
  end
  simple_path
end

def remove_relative(path)
  split_path = path.split('/')
  while split_path.include? '..'
    dotdot_index = split_path.index('..')
    split_path.delete_at(dotdot_index-1)  # remove the parent directory
    split_path.delete_at(dotdot_index-1)  # remove the '..'
  end
  split_path.join('/')
end

def register(params)
  # do nothing, no logstash params
end

# the filter method receives an event and must return a list of events.
def filter(event)
  proc_cwd = event.get("[process][cwd]")
  file_path = event.get("[file][path]")
  if proc_cwd != nil and file_path != nil and proc_cwd != '' and file_path != ''
    combined_path = proc_cwd + "/" + file_path
    combined_path = remove_duplicates(combined_path)
    combined_path = remove_relative(combined_path)
    event.set("combined_path", combined_path)
  end
  [event]
end