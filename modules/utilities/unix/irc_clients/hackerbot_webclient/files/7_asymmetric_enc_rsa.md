---
title: "Public-key Cryptography: Asymmetric Key Encryption with RSA"
author: ["Thomas Shaw", "Mo Hassan"]
license: "CC BY-SA 4.0"
description: "Learn how public-key cryptography works and where it fails: RSA by hand and with OpenSSL, digital signatures, hybrid encryption, certificate chains of trust, a real CA key-generation failure, and Diffie-Hellman key exchange."
overview: |
  Public-key cryptography (also known as asymmetric cryptography) lets two parties encrypt, sign and agree on keys without first sharing a secret. In this lab you will work through the RSA (Rivest-Shamir-Adleman) cipher by hand in Python, choosing the public exponent, finding the private exponent, and encrypting and decrypting messages, before using OpenSSL to generate RSA key pairs, encrypt data, sign and verify a message, and combine RSA with AES in the hybrid scheme that real systems use.

  You will then build a chain of trust of your own, with a root CA, an intermediate CA and a website certificate, and examine what happens when a certificate authority gets key generation wrong: in October 2026 the private keys of eight eMudhra intermediate CAs turned out to be tiny numbers (one of them was 4), and you will recover such a key yourself by brute force, then put the fix in place. The lab finishes with key exchange (Diffie-Hellman by hand) and a short look at how secure RSA is today, given the latest factoring records and quantum computing estimates.

  This is a Hackerbot lab. Work through the lab sheet, then when prompted interact with Hackerbot. Every value in Hackerbot's challenges is randomised, so your answers will differ from other students'.
tags: ["cryptography", "rsa", "asymmetric-encryption", "public-key", "digital-signatures", "pki", "certificates", "diffie-hellman", "openssl", "python"]
categories: ["cyber_security_landscape"]
lab_sheet_url: "https://cliffe.github.io/HacktivityLabSheets/labs/cyber_security_landscape/7-asymmetric-enc-rsa/"
type: ["ctf-lab", "hackerbot-lab", "lab-sheet"]
difficulty: "intermediate"
cybok:
  - ka: "AC"
    topic: "Algorithms, Schemes and Protocols"
    keywords: ["CRYPTOGRAPHY - ASYMMETRIC - RSA", "DIFFIE-HELLMAN ALGORITHM"]
  - ka: "AC"
    topic: "Public-Key Cryptography"
    keywords: ["public-key encryption", "public-key signatures", "RSA MODULUS", "RSA PROBLEM", "RSA TRANSFORM"]
  - ka: "AC"
    topic: "Key Management"
    keywords: ["key generation"]
  - ka: "AC"
    topic: "Cryptographic Implementation"
    keywords: ["Cryptographic Libraries", "ENCRYPTION - TOOLS"]
---

# Public-key Cryptography - Asymmetric Key Encryption with RSA

## Getting started {#getting-started}

### VMs in this lab {#vms-in-this-lab}

==action: Start these VMs== (if you haven't already):
- hackerbot_server (leave it running, you don't log into this)
- desktop

### Your login details for the "desktop" VM {#your-login-details}

==VM: On the desktop VM==, ==action: check who you're logged in as:==

```bash
whoami
```

Password: tiaspbiqe2r (**t**his **i**s **a** **s**ecure **p**assword **b**ut **i**s **q**uite **e**asy **2** **r**emember)

You won't login to the hackerbot_server, but the VM needs to be running to complete the lab.


### For marks in the module {#for-marks-in-the-module}

**You need to submit flags**. Note that the flags and the challenges in your VMs are different to other's in the class. Flags will be revealed to you as you complete challenges throughout the module. Flags look like this: `flag{somethingrandom}`. Submit your flags in Hacktivity to register your progress in the lab.

{% include hackerbot-intro.md role="check your work and challenge you with public-key cryptography tasks" chat="full" %}

## Purpose {#purpose}

The purpose of this lab is to understand public-key cryptography: how the RSA[^1] cipher works, how it is used in practice (encryption, digital signatures and hybrid encryption), how certificates build a chain of trust, what goes wrong when a key is generated badly, and how two parties can agree on a key over an open network.

[^1]: RSA is named after its three creators, Ron **R**ivest, Adi **S**hamir and Leonard **A**dleman, who published it in 1977.

## Public-key cryptography in a nutshell {#public-key-cryptography-in-a-nutshell}

With symmetric encryption (such as AES, in the previous lab) the same key encrypts and decrypts, so both parties must somehow share that key in secret before they can communicate. Public-key cryptography removes that requirement by giving each party a **key pair**: a **public key**, which can be given to anyone, and a **private key**, which never leaves its owner. The two keys are mathematically linked, but working out the private key from the public key is computationally infeasible, provided the keys were generated properly (a condition this lab returns to).

A key pair supports three operations that you will use in this lab:

- **Encryption**: anyone can encrypt a message with your public key, and only your private key can decrypt it.
- **Digital signatures**: you sign a message with your private key, and anyone can verify the signature with your public key, which shows that the message came from you and has not been altered.
- **Key exchange**: two parties combine their own private key with the other's public key to arrive at the same shared secret, which they then use as a symmetric key.

The weak point is trust in the public key itself. If Alice wants to send a message to Bob and she has a public key that claims to be Bob's, nothing in the mathematics tells her whether it really belongs to Bob or to an attacker in the middle of the conversation. **Public Key Infrastructure** (PKI) addresses this: a **Certification Authority** (CA), a trusted third party, checks an identity and signs a **certificate** binding that identity to a public key. The section on [chains of trust](#chains-of-trust) shows how this works in practice.

## How RSA works {#how-rsa-works}

1. Choose two large prime numbers, *p* and *q*, and keep them secret.

2. Compute the **modulus** *N*, which is part of both keys:

    N = p × q

3. Compute **φ(N)** (Euler's totient, written phi(N) or phiN in code):

    φ(N) = (p − 1)(q − 1)

4. Choose the **public exponent** *e*, which must satisfy two conditions:

    - 1 < e < φ(N)
    - e is **coprime** with φ(N), which means gcd(e, φ(N)) = 1 (their greatest common divisor is 1, so they share no factors)

5. Compute the **private exponent** *d*, the modular inverse of *e*:

    (e × d) mod φ(N) = 1, which is written d = e⁻¹ mod φ(N)

    The extended Euclidean algorithm computes *d* efficiently; Python does it for you with `pow(e, -1, phiN)`.

The **public key** is the pair (e, N) and the **private key** is (d, N). A message *M* (a number smaller than *N*) is encrypted and decrypted as follows:

- Encryption: C = M^e mod N
- Decryption: M = C^d mod N

RSA's security rests on the **RSA problem**: given only *N* and *e*, recovering *M* from *C* is believed to be as hard as factoring *N* back into *p* and *q*. Anyone who knows *p* and *q* can compute φ(N), and from it *d*, which is why the primes must stay secret and must be large (real keys use primes of 1024 bits or more each).

## RSA by hand, with Python {#rsa-by-hand-with-python}

This worked example uses tiny numbers so that you can follow every step; real RSA works in the same way with numbers hundreds of digits long.

==action: Open the Python REPL:==

```bash
python3
```

==action: Pick two primes and compute N and φ(N):==

```python
p = 7
q = 23
N = p * q
phiN = (p - 1) * (q - 1)
print(N, phiN)
```

This gives *N* = 161 and φ(N) = 132.

==action: List the values that could be used for e:==

```python
import math
[e for e in range(2, phiN) if math.gcd(e, phiN) == 1]
```

The list starts 5, 7, 13, 17, 19, ... Even numbers never appear, because φ(N) is always even; neither do multiples of 3 or 11, which also divide 132. This example uses e = 5, so the public key is (5, 161).

==action: Compute d and check it:==

```python
e = 5
d = pow(e, -1, phiN)
print(d, (e * d) % phiN)
```

This gives d = 53, and (5 × 53) mod 132 = 1 as required, so the private key is (53, 161).

==action: Encrypt and decrypt a number:==

```python
M = 42
C = pow(M, e, N)
print(C, pow(C, d, N))
```

`pow(M, e, N)` computes M^e mod N without first computing the enormous number M^e, which is how real RSA implementations handle numbers of thousands of bits. Here the ciphertext is 126, and decrypting it gives 42 again.

RSA works on numbers, so text has to be turned into numbers first. Python's `ord()` gives the number for a character (`ord('T')` is 84) and `chr()` does the reverse (`chr(84)` is `'T'`). ==action: Encrypt a short message one character at a time, then decrypt it:==

```python
ct = [pow(ord(c), e, N) for c in "The Hobbit"]
print(ct)
''.join(chr(pow(c, d, N)) for c in ct)
```

The ciphertext is `[7, 41, 54, 100, 151, 34, 140, 140, 119, 93]`, and decrypting it gives `'The Hobbit'` again.

> Question: Look at the two 140s in the ciphertext. Which letters are they, and what could an attacker learn from repeated numbers in a longer message?

Encrypting one character at a time in this way (**textbook RSA**) is really a substitution cipher: the same input always gives the same output, so frequency analysis breaks it. Real systems never use textbook RSA. They add random **padding** (OAEP, Optimal Asymmetric Encryption Padding) before encrypting, so that encrypting the same message twice gives different ciphertexts, and they encrypt a whole block at a time.

==action: Leave the REPL open==; you will need it for the next five challenges. Hackerbot gives you different numbers from the example, so use the code above with Hackerbot's values.

#### Hackerbot Attack #1 {#hackerbot-attack-1}

You can skip the bot to here, by saying **goto 1**.

> Hackerbot: Given p and q, pick out the valid values for e from a list of candidates.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the valid values, smallest first, separated by spaces*"==.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #2 {#hackerbot-attack-2}

You can skip the bot to here, by saying **goto 2**.

> Hackerbot: Given p, q and e, work out the private exponent d.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the value of d*"==.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #3 {#hackerbot-attack-3}

You can skip the bot to here, by saying **goto 3**.

> Hackerbot: Encrypt a number with a public key (e, N).

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the ciphertext*"==.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #4 {#hackerbot-attack-4}

You can skip the bot to here, by saying **goto 4**.

> Hackerbot: Decrypt a number, given p, q and e but not d.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the message*"==.

> Hint: Work out d from p, q and e first, as in the worked example.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #5 {#hackerbot-attack-5}

You can skip the bot to here, by saying **goto 5**.

> Hackerbot: Decrypt a word that was encrypted one character at a time with textbook RSA.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the word*"==.

> Hint: Put Hackerbot's numbers in a Python list, for example `ct = [12, 345, 67]`, then decrypt it with the `''.join(...)` line from the worked example, using Hackerbot's d and N.

Don't forget to ==action: save and submit any flags!==

## RSA with OpenSSL {#rsa-with-openssl}

OpenSSL is the toolkit behind much of the encryption on the internet, and its `openssl` command gives you real RSA, with real key sizes and proper padding. ==action: Leave Python== (`exit()`) and ==action: make a working directory:==

```bash
mkdir -p ~/rsa_example
cd ~/rsa_example
```

==action: Generate a 2048-bit RSA private key, then export its public key:==

```bash
openssl genrsa -out bob.priv 2048
openssl rsa -in bob.priv -pubout -out bob.pub
cat bob.priv
cat bob.pub
```

The private key file starts with `-----BEGIN PRIVATE KEY-----` (older versions of OpenSSL wrote `BEGIN RSA PRIVATE KEY`) and the public key file starts with `-----BEGIN PUBLIC KEY-----`. Both are PEM files: binary data encoded as base64, between header lines.

==action: Look inside the private key:==

```bash
openssl rsa -in bob.priv -noout -text
```

Every value from the worked example is here, at full size: the `modulus` (N), the `publicExponent` (e, almost always 65537), the `privateExponent` (d), and `prime1` and `prime2` (p and q). The public key holds only the modulus and the public exponent:

```bash
openssl rsa -pubin -in bob.pub -noout -text
```

> Warning: A private key must never be shared, and in practice it should be protected with a passphrase (add `-aes256` to `genrsa`). The keys in this lab have no passphrase, so that Hackerbot can check your work.

==action: Encrypt a message with Bob's public key, then decrypt it with his private key:==

```bash
echo 'Meet me at the library at 3' > msg.txt
openssl pkeyutl -encrypt -pubin -inkey bob.pub -in msg.txt -out msg.enc
openssl pkeyutl -decrypt -inkey bob.priv -in msg.enc
```

> Note: `-pubin` tells OpenSSL that the key file is a public key. Encrypt the same file twice and compare the two outputs (`cmp msg.enc msg2.enc`): they differ, because OpenSSL adds random padding, which is what textbook RSA lacks.

#### Hackerbot Attack #6 {#hackerbot-attack-6}

You can skip the bot to here, by saying **goto 6**.

> Hackerbot: Create an RSA key pair, with the file names Hackerbot gives you, in the directory Hackerbot names.

When you are ready for the bot to check your work, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #7 {#hackerbot-attack-7}

You can skip the bot to here, by saying **goto 7**.

> Hackerbot: Create another RSA key pair, then encrypt the text Hackerbot gives you with the public key.

When you are ready for the bot to check your work, ==action: say 'ready'== to Hackerbot.

> Hint: Put the text in a file first (`echo ==edit: the text from Hackerbot== > message.txt`), then encrypt the file.

Don't forget to ==action: save and submit any flags!==

## Digital signatures {#digital-signatures}

Encryption keeps a message secret; a **digital signature** proves who wrote it and that it has not been changed. The signer hashes the message (here with SHA-256) and transforms the hash with their private key; anyone with the public key can check that the signature matches the message. Changing a single character of the message changes its hash, so the signature no longer verifies.

==action: Sign a file with Bob's private key, and verify it with his public key:==

```bash
cd ~/rsa_example
echo 'I owe Alice 10 pounds' > iou.txt
openssl dgst -sha256 -sign bob.priv -out iou.sig iou.txt
openssl dgst -sha256 -verify bob.pub -signature iou.sig iou.txt
```

The last command prints `Verified OK`. ==action: Now change the message and verify it again:==

```bash
echo 'I owe Alice 1000 pounds' > iou.txt
openssl dgst -sha256 -verify bob.pub -signature iou.sig iou.txt
```

This time it reports a verification failure.

> Question: Who can create a valid signature for iou.txt, and who can check one? How does that differ from encryption?

#### Hackerbot Attack #8 {#hackerbot-attack-8}

You can skip the bot to here, by saying **goto 8**.

> Hackerbot: Sign the text Hackerbot gives you with your RSA private key, and leave the message, signature and public key where Hackerbot can check them.

When you are ready for the bot to check your work, ==action: say 'ready'== to Hackerbot.

> Hint: You can reuse the key pair from challenge 6: `cp ~/challenge_6/key.priv ~/challenge_6/key.pub ~/challenge_8/`

Don't forget to ==action: save and submit any flags!==

## Hybrid encryption {#hybrid-encryption}

RSA can only encrypt a message smaller than its modulus (less the padding, so at most 245 bytes with a 2048-bit key), and it is far slower than AES. ==action: See for yourself:==

```bash
cd ~/rsa_example
head -c 3000 /dev/urandom | base64 > report.txt
openssl pkeyutl -encrypt -pubin -inkey bob.pub -in report.txt -out report.rsa
```

OpenSSL refuses with an error. Real systems (TLS, PGP, S/MIME and encrypted disks among them) therefore use **hybrid encryption**: the data is encrypted with a random symmetric key, and only that small key is encrypted with RSA (known as **wrapping** the key).

==action: Encrypt the report with a random AES key, then wrap the AES key with Bob's public key:==

```bash
openssl rand -hex 32 > aes.key
openssl enc -aes-256-cbc -pbkdf2 -in report.txt -out report.enc -pass file:aes.key
openssl pkeyutl -encrypt -pubin -inkey bob.pub -in aes.key -out aes.key.enc
rm aes.key
```

Bob would receive report.enc and aes.key.enc. ==action: Decrypt them as Bob would:==

```bash
openssl pkeyutl -decrypt -inkey bob.priv -in aes.key.enc -out aes.key.bob
openssl enc -d -aes-256-cbc -pbkdf2 -in report.enc -pass file:aes.key.bob -out report.bob
cmp report.txt report.bob && echo 'Bob has the report'
```

> Note: Deleting aes.key matters: anyone who can read it can decrypt the report without Bob's private key.

#### Hackerbot Attack #9 {#hackerbot-attack-9}

You can skip the bot to here, by saying **goto 9**.

> Hackerbot: Encrypt a large file with AES, wrap the AES key with RSA, and leave Hackerbot what it needs to decrypt it.

When you are ready for the bot to check your work, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

## Chains of trust {#chains-of-trust}

A **certificate** is a public key plus an identity (such as a website's domain name), signed by a CA. Your browser trusts a few hundred **root CAs**, whose self-signed certificates ship with the browser or the operating system in a **root store**. Roots are too valuable to use every day, so their private keys are kept offline, and a root instead signs a small number of **intermediate CAs**, which sign the certificates for websites (**leaf** certificates). When you visit a website, it sends its leaf certificate and the intermediate; your browser checks each signature up the chain until it reaches a root it already trusts.

==action: Build a chain of your own:== a root CA, an intermediate CA signed by the root, and a leaf certificate for a website, signed by the intermediate.

```bash
mkdir -p ~/ca_example
cd ~/ca_example

# The root CA: a key, and a self-signed certificate
openssl genrsa -out root.key 2048
openssl req -x509 -new -key root.key -subj "/CN=My Root CA" -days 365 -out root.crt

# The intermediate CA: a key, a certificate signing request, and a certificate signed by the root
printf 'basicConstraints=critical,CA:TRUE\nkeyUsage=critical,keyCertSign,cRLSign\n' > ca.ext
openssl genrsa -out inter.key 2048
openssl req -new -key inter.key -subj "/CN=My Intermediate CA" -out inter.csr
openssl x509 -req -in inter.csr -CA root.crt -CAkey root.key -CAcreateserial -days 365 -extfile ca.ext -out inter.crt

# The leaf: a certificate for www.example.com, signed by the intermediate
printf 'basicConstraints=CA:FALSE\nsubjectAltName=DNS:www.example.com\n' > leaf.ext
openssl genrsa -out leaf.key 2048
openssl req -new -key leaf.key -subj "/CN=www.example.com" -out leaf.csr
openssl x509 -req -in leaf.csr -CA inter.crt -CAkey inter.key -CAcreateserial -days 365 -extfile leaf.ext -out leaf.crt
```

The ca.ext file marks the intermediate as a CA (`CA:TRUE`); without it, nothing the intermediate signs would be trusted. ==action: Check the chain:==

```bash
openssl verify -CAfile root.crt -untrusted inter.crt leaf.crt
openssl x509 -in leaf.crt -noout -subject -issuer
```

`-CAfile` names the certificate you trust (your root store) and `-untrusted` names the intermediate, which is checked rather than trusted. The first command prints `leaf.crt: OK`.

> Question: Run `openssl verify -CAfile root.crt leaf.crt` (without the intermediate). Why does it fail?

#### Hackerbot Attack #10 {#hackerbot-attack-10}

You can skip the bot to here, by saying **goto 10**.

> Hackerbot: Build a root CA, an intermediate CA and a leaf certificate for the website name Hackerbot gives you, in the directory Hackerbot names.

When you are ready for the bot to check your work, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

## When a CA gets key generation wrong {#when-a-ca-gets-key-generation-wrong}

Every certificate a CA has signed is only as trustworthy as that CA's private key. In October 2026 the security researcher Scott Helme found that the private keys of eight intermediate CAs run by eMudhra, an Indian CA trusted by every major browser, could be recovered from the CAs' public certificates in under a second. The keys were P-384 **elliptic-curve** keys, and a private key of that kind should be a random 384-bit number, one of roughly 10^115 possibilities; every one of eMudhra's eight fitted in a single byte, and one of them was the number 4. The keys came from a key-generation ceremony in April 2025 that produced almost no randomness, and they had sat in public certificates for 17 months before anyone noticed.

Anyone holding one of those keys could have signed a certificate for any website (a bank, an email provider, a government site), and every browser would have accepted it. The mathematics of elliptic-curve cryptography was sound throughout; the failure lay entirely in how the keys were generated.

### What a key of 4 looks like {#what-a-key-of-4-looks-like}

An elliptic-curve private key is a number *k*, and the public key is the point *k* × *G*, where *G* is a fixed point on the curve (P-384 here) that everyone knows. ==action: Make a key with eMudhra's private key of 4, using Python's pycryptodome library, and save both halves:==

```bash
mkdir -p ~/emudhra_example
cd ~/emudhra_example
python3
```

```python
from Cryptodome.PublicKey import ECC
weak = ECC.construct(curve='P-384', d=4)
open('emudhra4.pem', 'w').write(weak.export_key(format='PEM'))
open('emudhra4_pub.pem', 'w').write(weak.public_key().export_key(format='PEM'))
exit()
```

==action: Look at the private key file:==

```bash
cat emudhra4.pem
```

It looks much like any other private key:

```
-----BEGIN PRIVATE KEY-----
MIG2AgEAMBAGByqGSM49AgEGBSuBBAAiBIGeMIGbAgEBBDAAAAAAAAAAAAAAAAAA
AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAShZANiAAQTglHNUqyS
mMHIqtl3Mh3rl+cJvQtMoKylXcitUdz8nRWJoVl+OlEg4e/WMcY+GDXKyuKYaaYu
FjHoooGBq1ZhbcRdkYq8CfOrDmPPeSqk3O1zh743u6VpVJ8cArJw7Wc=
-----END PRIVATE KEY-----
```

until you notice the long run of `A`s, which is how base64 writes zero bytes. ==action: Ask OpenSSL to show what is inside:==

```bash
openssl pkey -in emudhra4.pem -noout -text | head -7
```

```
Private-Key: (384 bit)
priv:
    00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:
    00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:
    00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:
    00:00:04
pub:
```

The `priv:` value is the private key: 48 bytes, 47 of them zero, then `04`. This is a valid 384-bit private key in the format every CA uses, and its secret is the number 4.

### Brute-forcing a tiny key {#brute-forcing-a-tiny-key}

Recovering *k* from the public key is the elliptic-curve equivalent of factoring, and is infeasible when *k* is a random 384-bit number. When *k* is small, you can simply try every value in turn: compute 1 × *G*, 2 × *G*, 3 × *G*, and so on, until one matches the public key. ==action: Brute-force the public key you just saved:==

```bash
python3
```

```python
from Cryptodome.PublicKey import ECC
pub = ECC.import_key(open('emudhra4_pub.pem').read())
for k in range(1, 100000):
    if ECC.construct(curve='P-384', d=k).pointQ == pub.pointQ:
        print('Found the private key:', k)
        break

```

> Note: Press Enter on the blank line to run the loop. It finds 4 straight away.

#### Hackerbot Attack #11 {#hackerbot-attack-11}

You can skip the bot to here, by saying **goto 11**.

> Hackerbot: Hackerbot leaves you a root CA certificate and an intermediate CA certificate whose private key is one of eMudhra's; brute-force it.

==action: Say 'ready'== to Hackerbot, which puts the two certificates in ~/challenge_11. ==action: Look at the intermediate, extract its public key, and brute-force it with the loop above:==

```bash
cd ~/challenge_11
openssl x509 -in lab_trust_inter.crt -noout -text | head -20
openssl x509 -in lab_trust_inter.crt -noout -pubkey > inter_pub.pem
python3
```

Use `'inter_pub.pem'` in place of `'emudhra4_pub.pem'` in the brute-force code.

There is a quiz to complete: ==action: answer with "answer *the private key*"==.

Don't forget to ==action: save and submit any flags!==

==action: See the consequence for yourself.== Still in Python, save the key you recovered as a PEM file, replacing `k` with your number if you have left the loop:

```python
key = ECC.construct(curve='P-384', d=k)
open('recovered.pem', 'w').write(key.export_key(format='PEM'))
exit()
```

Then sign a certificate for a bank's website with the stolen key, and check it against the CA's root:

```bash
openssl genrsa -out fake.key 2048
openssl req -new -key fake.key -subj "/CN=www.bank.example" -out fake.csr
openssl x509 -req -in fake.csr -CA lab_trust_inter.crt -CAkey recovered.pem -CAcreateserial -days 30 -out fake.crt
openssl verify -CAfile lab_trust_root.crt -untrusted lab_trust_inter.crt fake.crt
```

`fake.crt: OK`: any client that trusted that root would accept your certificate for the bank.

## The fix {#the-fix}

A CA whose key has leaked cannot fix its existing certificates; the response has to stop clients from trusting the key, and the CA has to generate its keys properly in future. In eMudhra's case the browsers acted within hours of the report, and each part of the response below is something you can try.

### Block the key {#block-the-key}

Chrome did not block eMudhra's certificates one at a time; it blocked the keys, using each intermediate's **SPKI hash**, the SHA-256 hash of its Subject Public Key Info (the public key, in the form a certificate stores it). Within two hours of the report Chrome had pushed the eight hashes to every browser through CRLSet, its own list of blocked certificates and keys, so every certificate containing one of those public keys was blocked at once, however many copies or reissues existed.

==action: Work out the SPKI hash of the leaf certificate you made earlier:==

```bash
cd ~/ca_example
openssl x509 -in leaf.crt -noout -pubkey | openssl pkey -pubin -outform der | sha256sum
```

The first command extracts the public key from the certificate, the second converts it to the binary (DER) form that is hashed, and `sha256sum` hashes it. Chrome writes the same hash in base64, which you can get by replacing `sha256sum` with `openssl dgst -sha256 -binary | base64`.

#### Hackerbot Attack #12 {#hackerbot-attack-12}

You can skip the bot to here, by saying **goto 12**.

> Hackerbot: Work out the SPKI hash that would block the weak intermediate CA.

When you are ready, ==action: say 'ready'== to Hackerbot (it puts the files back in ~/challenge_11 if they are missing).

There is a quiz to complete: ==action: answer with "answer *the SPKI hash*"== (hex or base64).

Don't forget to ==action: save and submit any flags!==

### Revoke, and remove the root {#revoke-and-remove-the-root}

eMudhra revoked the eight intermediate certificates the day after the report, giving the reason `caCompromise`. Mozilla and Apple pushed updates, Microsoft released an out-of-band Windows update that removed the certificates, and Chrome went on to remove eMudhra's root from its root store, so that nothing the root had signed would be trusted. Within days eMudhra announced that it was leaving the public TLS certificate business.

> Question: Revoking a certificate only helps if clients check for revocation, and many programs never do. Why did the browsers block eMudhra's keys, and later remove its root, rather than rely on revocation?

### Generate keys properly {#generate-keys-properly}

The lasting fix is a key that nobody can guess. ==action: Generate a P-384 key the normal way, and compare it with the key of 4:==

```bash
cd ~/emudhra_example
openssl genpkey -algorithm EC -pkeyopt ec_paramgen_curve:P-384 -out good.key
openssl pkey -in good.key -noout -text | head -7
```

This time `priv:` is 48 random bytes, drawn from the operating system's cryptographically secure random number generator, and brute force would need to try around 10^115 values. CAs are meant to generate their keys inside a **hardware security module** (HSM), a tamper-resistant device that creates and stores keys and never releases them, during a witnessed and recorded key ceremony. eMudhra's failure shows that the result still needs checking: a single look at the private key value, or at the public key against a list of trivially small keys, would have caught it.

A related change is under way across the whole web. In April 2025 the CA/Browser Forum (the CAs and browser makers who agree the rules for website certificates) voted to cut the maximum lifetime of a public TLS certificate from 398 days to 200 days from March 2026, 100 days from March 2027 and 47 days from March 2029, which limits how long a mis-issued certificate, or a certificate whose key has been stolen, stays useful.

## Key exchange {#key-exchange}

Public-key encryption can deliver a symmetric key, as you did in the hybrid encryption section, but modern protocols normally use **key exchange** instead: both parties contribute to the key, and it is never sent at all. **Diffie-Hellman** (DH), published in 1976, was the first public-key scheme of any kind.

1. Alice and Bob agree, in public, on a prime *p* and a **generator** *g* (a number whose powers mod *p* produce every value from 1 to *p* − 1).
2. Alice picks a private value *a* and sends Bob A = g^a mod p.
3. Bob picks a private value *b* and sends Alice B = g^b mod p.
4. Alice computes B^a mod p and Bob computes A^b mod p. Both equal g^(ab) mod p, which is their shared secret.

An eavesdropper sees *p*, *g*, *A* and *B*, but recovering *a* or *b* from them (the **discrete logarithm problem**) is infeasible for large numbers.

==action: Work through an example in Python:==

```python
p, g = 23, 5
a, b = 6, 15
A = pow(g, a, p)
B = pow(g, b, p)
print(A, B, pow(B, a, p), pow(A, b, p))
```

Alice sends 8, Bob sends 19, and both arrive at the shared secret 2.

#### Hackerbot Attack #13 {#hackerbot-attack-13}

You can skip the bot to here, by saying **goto 13**. This is the final challenge.

> Hackerbot: Hackerbot gives you p, g, your private value and its own public value; work out your public value and the shared secret.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *your public value A* *the shared secret*"== (two numbers separated by a space).

Don't forget to ==action: save and submit any flags!==

### Elliptic-curve key exchange and forward secrecy {#x25519}

Today's protocols mostly use **elliptic-curve Diffie-Hellman**, in particular **X25519**, which follows the same steps on an elliptic curve with much shorter keys. TLS 1.3 uses a fresh key exchange for every connection, which gives **forward secrecy**: even if a server's long-term private key is stolen later, recorded traffic cannot be decrypted, because the keys that protected it no longer exist. That is why TLS 1.3 no longer supports the older approach of encrypting the session key with the server's RSA key.

## How secure is RSA today? {#how-secure-is-rsa-today}

The best known way to factor an RSA modulus is the **general number field sieve**, and the public record shows how far it reaches. RSA-250 (829 bits) was factored in 2020, using about 2,700 CPU core-years. In September 2026 two older challenge numbers fell within sixteen days of each other: RSA-260 (862 bits), which took about 13.5 GPU-years, and RSA-896 (896 bits), on roughly 2,000 GPUs over ten days. Both teams used AI agents to engineer and run the computation, but neither used a new algorithm. The effort needed grows far faster than the key size, so these results do not threaten the 2048-bit keys in use today; keys of 1024 bits, however, should be regarded as within reach of a well-funded attacker.

Quantum computers are a different matter. Shor's algorithm would factor RSA moduli, and solve the discrete logarithm problems behind Diffie-Hellman and elliptic-curve cryptography, efficiently. In 2025 Craig Gidney estimated that a 2048-bit RSA key could be factored in under a week by a quantum computer with fewer than a million noisy qubits, around twenty times fewer than his 2019 estimate. No such machine exists, but data encrypted today can be recorded and decrypted later, so the migration to **post-quantum cryptography** has already started: browsers and TLS libraries now combine X25519 with ML-KEM, a post-quantum key exchange standardised by NIST in 2024, and the UK's NCSC has set 2035 as the date by which organisations should complete their migration.

> Question: RSA-896 was factored in 2026. Does that mean RSA-2048 is next? What evidence would you look for before deciding when to stop using RSA?

## Resources {#resources}

[1] Terence Spies. "Chapter 39 - Public Key Infrastructure". In: *Computer and Information Security Handbook (Second Edition).* Ed. by John R. Vacca. Boston: Morgan Kaufmann, 2013, p. 703. DOI: https://doi.org/10.1016/B978-0-12-394397-2.00039-8

[2] Tiffany Hyun-Jin Kim et al. "Accountable key infrastructure (AKI) a proposal for a public-key validation infrastructure". In: *Proceedings of the 22nd international conference on World Wide Web.* 2013, pp. 679-690.

[3] Johannes Buchmann et al. *Introduction to public key infrastructures.* Vol. 36. Springer, 2013.

[4] Douglas R. Stinson and Maura B. Paterson. *Cryptography: Theory and Practice*. 4th edition. CRC Press, 2019.

[5] Keith M. Martin. *Everyday Cryptography*. 2nd edition. Oxford University Press, 2017.

[6] Google. "Protecting Chrome and Android users from compromised eMudhra intermediate CA certificates". October 2026. https://blog.google/security/protecting-chrome-and-android-users-from-compromised-emudhra-intermediate-ca-certificates/

[7] Microsoft Security Response Center. "Microsoft's response to compromised eMudhra certificate authority keys". October 2026. https://www.microsoft.com/en-us/msrc/blog/2026/10/microsoft-response-to-compromised-emudhra-certificate-authority-keys

[8] The Register. "Indian certification authority eMudhra's private keys compromised". 20 October 2026. https://www.theregister.com/security/2026/10/20/indian-certification-authority-emudhras-private-keys-compromised-one-of-them-was-the-number-4/5304312

[9] Craig Gidney. "How to factor 2048 bit RSA integers with less than a million noisy qubits". arXiv:2505.15917, 2025. https://arxiv.org/abs/2505.15917

[10] RSA Factoring Challenge. Wikipedia. https://en.wikipedia.org/wiki/RSA_Factoring_Challenge
