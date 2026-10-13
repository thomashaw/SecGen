# Sets combined_path: the absolute, normalised path of the file an audit event
# refers to, built from the process working directory and the path the program
# passed to the system call. Normalisation is lexical (symbolic links are not
# resolved, since the analysis server cannot see the monitored host's file
# system): empty segments ('//') and '.' are dropped and '..' removes the
# preceding segment, so the result never contains '//', '/./' or '/../'.

# An absolute file path is complete on its own; a relative one is relative to
# the process working directory.
def join_path(cwd, file_path)
  if file_path.start_with?('/')
    file_path
  elsif cwd != nil and cwd != ''
    cwd + '/' + file_path
  end
end

def normalise_path(path)
  segments = []
  path.split('/').each do |segment|
    case segment
    when '', '.'
      next
    when '..'
      segments.pop  # '..' at the root stays at the root
    else
      segments << segment
    end
  end
  '/' + segments.join('/')
end

def register(params)
  # do nothing, no logstash params
end

# the filter method receives an event and must return a list of events.
def filter(event)
  proc_cwd = event.get("[process][working_directory]")
  file_path = event.get("[file][path]")
  if file_path != nil and file_path != ''
    joined = join_path(proc_cwd, file_path)
    event.set("combined_path", normalise_path(joined)) if joined
  end
  [event]
end
