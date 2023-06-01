puts "SecGen nic definitions that use the special IP_addresses datastore should define networks in the same sequence, so that we can automatically identify IP addrs for VMs"

directory_path = ARGV[0]

# Ensure directory path is provided
if directory_path.nil?
  puts "Please provide as an argument the directory path to recursively test scenario xml files."
  exit
end

# Retrieve all XML files in the directory
xml_files = Dir.glob("#{directory_path}/**/*.xml")

# Regular expression pattern to match network sections
network_regex = /<network\b[^>]*>(.*?)<\/network>/m

# Initialize counters
processed_files_count = 0
out_of_sequence_count = 0

# Iterate over each XML file
xml_files.each do |file_path|
  puts "Processing file: #{file_path}"
  processed_files_count += 1

  begin
    file_contents = File.read(file_path)

    # Check for sequential access numbers across all the network sections
    sequential = true
    last_access = -1
    lines_with_access_numbers = []

    file_contents.scan(network_regex) do |network_match|
      network_section = network_match[0]
      network_lines = network_section.split("\n")

      network_lines.each do |line|
        match = line.match(/<datastore\s+access="(\d+)">.*?<\/datastore>/)
        next unless match

        access_number = match[1].to_i

        lines_with_access_numbers << line.strip

        if access_number != last_access + 1
          puts "  \e[31mWarning:\e[0m Access numbers are not sequential in #{file_path}. Expected #{last_access + 1}, got #{access_number}"
          sequential = false
          out_of_sequence_count += 1
          break
        end

        last_access = access_number
      end

      if !sequential
        break
      end
    end

    if sequential
      lines_with_access_numbers = []
    end

    unless lines_with_access_numbers.empty?
      puts "Lines with access numbers:"
      lines_with_access_numbers.each { |line| puts "  #{line}" }
    end

  rescue StandardError => e
    puts "Error processing file #{file_path}: #{e.message}"
  end

  puts "-" * 40
end

puts "Processed files: #{processed_files_count}"
puts "Out of sequence files: #{out_of_sequence_count}"
