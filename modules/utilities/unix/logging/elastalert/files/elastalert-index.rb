require 'fileutils'
require 'open3'

@registered_file = '/ea'

def already_registered?
  File.file? @registered_file
end

until already_registered?
  stdout, _, _ = Open3.capture3("/usr/local/bin/elastalert-create-index")
  if stdout.include? 'New index elastalert_status created' or stdout.include? 'Index elastalert_status already exists'
    FileUtils.touch @registered_file
  end
  sleep(15)
end

exit(0)