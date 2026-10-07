require 'fileutils'

# Tees everything SecGen writes to stdout/stderr into a log file, while still
# showing it on the terminal. The redirect is done at the file descriptor level,
# so output from child processes (vagrant, generators, puppet) is captured too.
# The terminal gets the raw output; the log file gets it with colour codes removed.
class SecGenLog
  ANSI_ESCAPE = /\e\[[0-9;?]*[A-Za-z]/
  SECRET_OPTIONS = %w(--ovirtpass --proxmoxpass --esxipass)

  class << self
    attr_reader :path
    attr_accessor :project_dir
  end

  # @param [String] path -- log file to append to
  # @param [Array] args -- command line arguments, recorded in the log header (passwords redacted)
  def self.start(path, args)
    return if @path
    FileUtils.mkdir_p(File.dirname(path))
    @path = path
    @file = File.open(path, 'a')
    @file.sync = true
    @mutex = Mutex.new
    @started = Time.now

    @file.write("SecGen log started #{@started}\n")
    @file.write("Command: secgen.rb #{redact(args).join(' ')}\n")
    commit = `git -C '#{ROOT_DIR}' rev-parse --short HEAD 2>/dev/null`.strip
    branch = `git -C '#{ROOT_DIR}' rev-parse --abbrev-ref HEAD 2>/dev/null`.strip
    @file.write("Git: #{branch} #{commit}\n") unless commit.empty?
    @file.write('~' * 47 + "\n")

    # stdout and stderr share one pipe (like 2>&1) so the log keeps their ordering
    @originals = [$stdout, $stderr].map { |stream| [stream, stream.dup] }
    terminal = @originals.first[1]
    reader, writer = IO.pipe
    @originals.each do |stream, _|
      stream.reopen(writer)
      stream.sync = true
    end
    writer.close
    @thread = Thread.new { pump(reader, terminal) }

    at_exit { finish($!) }
  end

  def self.finish(exception = nil)
    return unless @file && !@file.closed?
    # Restoring the streams closes our pipe write ends, so the reader threads see EOF
    @originals.each do |stream, original|
      stream.flush
      stream.reopen(original)
    end
    # A detached child process could still hold a pipe open; don't hang on exit
    @thread.join(5)

    @mutex.synchronize do
      if exception && !exception.is_a?(SystemExit)
        @file.write("\nUncaught exception: #{exception.class}: #{exception.message}\n")
        @file.write("#{(exception.backtrace || []).join("\n")}\n")
      end
      status = exception.is_a?(SystemExit) ? exception.status : (exception ? 1 : 0)
      @file.write("\nSecGen log finished #{Time.now} (exit status #{status}, #{(Time.now - @started).round}s)\n")
      @file.close
    end
    $stdout.puts Print.grey("SecGen output logged to #{@path}")

    if @project_dir && File.directory?(@project_dir)
      FileUtils.mkdir_p("#{@project_dir}/logs")
      FileUtils.cp(@path, "#{@project_dir}/logs/#{File.basename(@path)}")
    end
  end

  def self.pump(reader, original)
    buffer = ''
    loop do
      chunk = reader.readpartial(4096)
      original.write(chunk)
      original.flush
      buffer << chunk
      # Only log whole lines, so colour codes aren't split between writes
      if (last_newline = buffer.rindex("\n"))
        log(buffer[0..last_newline])
        buffer = buffer[(last_newline + 1)..-1]
      end
    end
  rescue EOFError, IOError
    log(buffer) unless buffer.empty?
  end

  def self.log(text)
    text = text.dup.force_encoding('UTF-8').scrub
    text = text.gsub(ANSI_ESCAPE, '').gsub("\r\n", "\n")
    @mutex.synchronize { @file.write(text) unless @file.closed? }
  end

  def self.redact(args)
    args.each_with_index.map do |arg, i|
      if i > 0 && SECRET_OPTIONS.include?(args[i - 1])
        '********'
      elsif arg.include?('=') && SECRET_OPTIONS.include?(arg.split('=').first)
        "#{arg.split('=').first}=********"
      else
        arg
      end
    end
  end
end
