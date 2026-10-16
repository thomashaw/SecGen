# Measure victim-minus-server clock offset with ICMP timestamp requests (RFC 792
# types 13/14), the method clockdiff uses. Run as root on the server:
#   ruby icmp_offset.rb 172.16.0.3 [samples]
# Timestamps are ms since midnight UTC, so resolution is 1 ms; the sample with
# the smallest round trip gives the tightest bound (offset +/- rtt/2).
require 'socket'

def ms_now
  t = Time.now.utc
  ((t.to_i % 86_400) * 1000) + (t.nsec / 1_000_000.0)
end

def checksum(data)
  sum = data.unpack('n*').sum
  sum += data[-1].ord << 8 if data.bytesize.odd?
  sum = (sum & 0xffff) + (sum >> 16) while sum > 0xffff
  ~sum & 0xffff
end

target = ARGV[0] or abort 'usage: icmp_offset.rb HOST [samples]'
samples = (ARGV[1] || 30).to_i
sock = Socket.new(:INET, :RAW, Socket::IPPROTO_ICMP)
addr = Socket.sockaddr_in(0, target)
id = Process.pid & 0xffff
results = []
samples.times do |seq|
  t1 = ms_now
  body = [13, 0, 0, id, seq, t1.floor, 0, 0].pack('CCnnnNNN')
  body[2, 2] = [checksum(body)].pack('n')
  sock.send(body, 0, addr)
  loop do
    break unless IO.select([sock], nil, nil, 1)
    pkt = sock.recv(1500)
    t4 = ms_now
    ihl = (pkt.getbyte(0) & 0x0f) * 4
    type, _code, _ck, rid, rseq, _orig, recv, xmit = pkt[ihl, 20].unpack('CCnnnNNN')
    next unless type == 14 && rid == id && rseq == seq
    rtt = t4 - t1
    offset = ((recv + xmit) / 2.0) - ((t1 + t4) / 2.0)
    results << [rtt, offset, recv, xmit]
    break
  end
  sleep 0.1
end
abort 'no ICMP timestamp replies (blocked or unsupported)' if results.empty?
results.sort_by!(&:first)
offsets = results.map { |r| r[1] }.sort
puts "replies: #{results.size}/#{samples}"
puts format('best sample: rtt %.3f ms, victim - server offset %.1f ms (+/- %.1f ms incl. 1 ms resolution)',
            results[0][0], results[0][1], results[0][0] / 2 + 1)
puts format('all samples: offset min %.1f / median %.1f / max %.1f ms',
            offsets.first, offsets[offsets.size / 2], offsets.last)
