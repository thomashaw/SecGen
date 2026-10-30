#!/usr/bin/ruby
require_relative '../../../../../../lib/objects/local_hackerbot_config_generator.rb'
require 'openssl'
require 'securerandom'
require 'base64'

class AsymmetricEncRSA < HackerbotConfigGenerator

  attr_accessor :desktop_ip
  attr_accessor :hackerbot_server_ip
  attr_accessor :secret_word

  def initialize
    super
    self.module_name = 'Hackerbot Config Generator Asymmetric/RSA'
    self.title = 'Public-Key Cryptography with RSA'

    self.local_dir = File.expand_path('../../', __FILE__)
    self.templates_path = "#{self.local_dir}/templates/"
    self.config_template_path = "#{self.local_dir}/templates/asymmetric_enc_rsa_lab.xml.erb"
    self.desktop_ip = ''
    self.hackerbot_server_ip = ''
    self.secret_word = ''
  end

  def get_options_array
    super + [['--desktop_ip', GetoptLong::REQUIRED_ARGUMENT],
             ['--hackerbot_server_ip', GetoptLong::REQUIRED_ARGUMENT],
             ['--secret_word', GetoptLong::REQUIRED_ARGUMENT]]
  end

  def process_options(opt, arg)
    super
    case opt
      when '--desktop_ip'
        self.desktop_ip << arg;
      when '--hackerbot_server_ip'
        self.hackerbot_server_ip << arg;
      when '--secret_word'
        self.secret_word << arg;
    end
  end
end

# ---------------------------------------------------------------------------------------------
# Toy RSA and Diffie-Hellman values, picked at build time so every lab instance differs.
# Numbers stay small enough to work through in the Python REPL.
# ---------------------------------------------------------------------------------------------
TOY_PRIMES = [11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97]
DH_PRIMES = [83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179, 181, 191, 193, 197, 199]

def rand_between(lo, hi)
  lo + SecureRandom.random_number(hi - lo + 1)
end

def mod_inverse(a, m)
  old_r, r = a % m, m
  old_s, s = 1, 0
  while r != 0
    q = old_r / r
    old_r, r = r, old_r - q * r
    old_s, s = s, old_s - q * s
  end
  raise "#{a} has no inverse mod #{m}" unless old_r == 1
  old_s % m
end

def prime_factors(n)
  factors = []
  f = 2
  while f * f <= n
    while n % f == 0
      factors << f unless factors.include?(f)
      n /= f
    end
    f += 1
  end
  factors << n if n > 1
  factors
end

# A toy key pair: p != q, e coprime with phi(N), and d different from e (so knowing e doesn't give d away).
# An RSA key leaves (1 + gcd(e-1, p-1)) * (1 + gcd(e-1, q-1)) messages unchanged; some e (e.g. e = 1 + lcm(p-1, q-1))
# leave every message unchanged, so keep only keys with a handful of those.
def toy_rsa_key
  loop do
    p, q = TOY_PRIMES.sample(2, random: SecureRandom)
    n = p * q
    phi = (p - 1) * (q - 1)
    e = rand_between(3, phi - 1)
    next unless e.gcd(phi) == 1
    next if (1 + (e - 1).gcd(p - 1)) * (1 + (e - 1).gcd(q - 1)) > 9
    d = mod_inverse(e, phi)
    next if d == e
    return { p: p, q: q, n: n, phi: phi, e: e, d: d }
  end
end

# A message m with 1 < m < N - 1 that doesn't encrypt to itself.
def toy_message(key)
  loop do
    m = rand_between(2, key[:n] - 2)
    next unless m.gcd(key[:n]) == 1
    return m unless m.pow(key[:e], key[:n]) == m
  end
end

# Six candidate values for e: three valid (coprime with phi), three not.
def e_candidates(key)
  phi = key[:phi]
  limit = [phi - 1, 150].min
  valid = (3..limit).select { |x| x.gcd(phi) == 1 }.sample(3, random: SecureRandom)
  odd_invalid = (3..limit).select { |x| x.odd? && x.gcd(phi) != 1 }
  even = (4..limit).select(&:even?)
  invalid = odd_invalid.sample([2, odd_invalid.size].min, random: SecureRandom)
  invalid += even.sample(3 - invalid.size, random: SecureRandom)
  { candidates: (valid + invalid).sort, valid: valid.sort }
end

def encrypt_rsa(message, e, n)
  message.chars.map { |c| c.ord.pow(e, n) }.join(' ')
end

def primitive_root(p)
  factors = prime_factors(p - 1)
  (2...p).find { |g| factors.all? { |f| g.pow((p - 1) / f, p) != 1 } }
end

def toy_dh
  p = DH_PRIMES.sample(random: SecureRandom)
  g = primitive_root(p)
  loop do
    a = rand_between(2, p - 2)
    b = rand_between(2, p - 2)
    big_a = g.pow(a, p)
    big_b = g.pow(b, p)
    s = big_b.pow(a, p)
    next if s == 1 || big_a == big_b
    return { p: p, g: g, a: a, b: b, A: big_a, B: big_b, s: s }
  end
end

# ---------------------------------------------------------------------------------------------
# The "eMudhra" challenges: a P-384 intermediate CA whose private key is a tiny number.
# Keys are built from DER so this works with both the openssl 2.x and 3.x gems (3.x pkeys are immutable).
# ---------------------------------------------------------------------------------------------
EC_CURVE = 'secp384r1'

def ec_spki_der(point)
  OpenSSL::ASN1::Sequence([
    OpenSSL::ASN1::Sequence([OpenSSL::ASN1::ObjectId('id-ecPublicKey'), OpenSSL::ASN1::ObjectId(EC_CURVE)]),
    OpenSSL::ASN1::BitString(point.to_octet_string(:uncompressed))
  ]).to_der
end

def ec_key_from_scalar(k)
  group = OpenSSL::PKey::EC::Group.new(EC_CURVE)
  point = group.generator.mul(OpenSSL::BN.new(k))
  der = OpenSSL::ASN1::Sequence([
    OpenSSL::ASN1::Integer(1),
    OpenSSL::ASN1::OctetString(OpenSSL::BN.new(k).to_s(2).rjust(48, "\x00")),
    OpenSSL::ASN1::ObjectId(EC_CURVE, 0, :EXPLICIT),
    OpenSSL::ASN1::BitString(point.to_octet_string(:uncompressed), 1, :EXPLICIT)
  ]).to_der
  { key: OpenSSL::PKey::EC.new(der), spki: ec_spki_der(point) }
end

def make_ca_cert(subject, key, issuer_cert, issuer_key, serial)
  cert = OpenSSL::X509::Certificate.new
  cert.version = 2
  cert.serial = serial
  cert.subject = OpenSSL::X509::Name.parse(subject)
  cert.issuer = issuer_cert ? issuer_cert.subject : cert.subject
  cert.public_key = key
  cert.not_before = Time.now - 86400
  cert.not_after = Time.now + 5 * 365 * 86400
  ef = OpenSSL::X509::ExtensionFactory.new
  ef.subject_certificate = cert
  ef.issuer_certificate = issuer_cert || cert
  cert.add_extension(ef.create_extension('basicConstraints', 'CA:TRUE', true))
  cert.add_extension(ef.create_extension('keyUsage', 'keyCertSign, cRLSign', true))
  cert.add_extension(ef.create_extension('subjectKeyIdentifier', 'hash', false))
  cert.sign(issuer_key, OpenSSL::Digest::SHA384.new)
  cert
end

def weak_ca(private_scalar)
  root_key = OpenSSL::PKey::EC.new(EC_CURVE)
  root_key.generate_key!
  root = make_ca_cert('/C=GB/O=Lab Trust Services/CN=Lab Trust ECC Root CA', root_key, nil, root_key, SecureRandom.random_number(2**63))
  inter = ec_key_from_scalar(private_scalar)
  inter_cert = make_ca_cert('/C=GB/O=Lab Trust Services/CN=Lab Trust ECC Issuing CA 1', inter[:key], root, root_key, SecureRandom.random_number(2**63))
  spki_sha256 = OpenSSL::Digest::SHA256.digest(inter[:spki])
  { root_pem: root.to_pem, inter_pem: inter_cert.to_pem,
    spki_hex: spki_sha256.unpack1('H*'), spki_b64: Base64.strict_encode64(spki_sha256) }
end

# ---------------------------------------------------------------------------------------------
# Bot commands. Check scripts are shipped base64-encoded (no quoting or XML escaping problems) and
# piped into the bash the bot gets on the desktop. Each prints HB: markers that the conditions match.
# ---------------------------------------------------------------------------------------------
# The script runs from a temp file with stdin from /dev/null, so no command in it can read the rest of the script.
# The result goes into the bot's XML, so it is XML-escaped (the bot reads the '<' back as plain text).
def on_desktop(script)
  "f=$(mktemp); echo #{Base64.strict_encode64(script)} | base64 -d > $f; bash $f </dev/null 2>/dev/null; rm -f $f".encode(xml: :text)
end

# Writes a file as the student, so they own it: write_file_script('/home/u/x.pem', pem, 'u')
def write_file_script(path, content, user)
  "echo #{Base64.strict_encode64(content)} | base64 -d > #{path}; chown #{user}: #{path}\n"
end

AsymmetricEncRSA.new.run
