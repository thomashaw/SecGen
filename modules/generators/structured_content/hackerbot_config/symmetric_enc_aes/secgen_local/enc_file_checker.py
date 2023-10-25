from Cryptodome.Cipher import AES
from Cryptodome.Util.Padding import pad,unpad
with open("<%= $enc_file_python_dest %>", "rb") as enc_target_file:
  source = enc_target_file.read();
k = b"<%=$enc_file_python_key %>"
e = AES.new(k, AES.MODE_CBC)
target_file_ct = e.encrypt(pad(enc_target_file,16))

with open("<%$enc_file_python_dest %>","rb") as student_ct_file:
  student_ct = ct_file.read()

print(target_file_ct == student_ct)