---
title: "Public-key Cryptography: Asymmetric Key Encryption with RSA"
author: ["Thomas Shaw", "Mo Hassan"]
license: "CC BY-SA 4.0"
description: "Learn how public-key cryptography works and where it fails: RSA by hand and with OpenSSL, digital signatures, Diffie-Hellman key exchange, certificate chains of trust, and a real CA key-generation failure you exploit yourself."
overview: |
  Public-key cryptography (also known as asymmetric cryptography) lets two parties encrypt, sign and agree on keys without first sharing a secret. In this lab you will work through the RSA (Rivest-Shamir-Adleman) cipher by hand in Python, choosing the public exponent, finding the private exponent, and encrypting and decrypting messages, before using OpenSSL to generate RSA key pairs, encrypt data, and sign and verify a message. You will then agree a shared secret with Diffie-Hellman key exchange.

  Finally you will see how certificates and chains of trust tell you who you are talking to, and what happens when a certificate authority gets key generation wrong: in October 2026 the private keys of eight eMudhra intermediate CAs turned out to be tiny numbers (one of them was 4). You will recover such a key yourself by brute force and use it to forge a certificate for a bank's website.

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

The purpose of this lab is to understand public-key cryptography: how the RSA[^1] cipher works, how it is used in practice (encryption and digital signatures), how two parties can agree on a key over an open network, how certificates build a chain of trust, and what goes wrong when a key is generated badly.

[^1]: RSA is named after its three creators, Ron **R**ivest, Adi **S**hamir and Leonard **A**dleman, who published it in 1977.

## Public-key cryptography in a nutshell {#public-key-cryptography-in-a-nutshell}

With symmetric encryption (such as AES, in the previous lab) the same key encrypts and decrypts, so both parties must somehow share that key in secret before they can communicate. Public-key cryptography removes that requirement by giving each party a **key pair**: a **public key**, which can be given to anyone, and a **private key**, which never leaves its owner. The two keys are mathematically linked, but working out the private key from the public key is computationally infeasible, provided the keys were generated properly (a condition this lab returns to).

A key pair supports three operations that you will use in this lab:

- **Encryption**: anyone can encrypt a message with your public key, and only your private key can decrypt it.
- **Digital signatures**: you sign a message with your private key, and anyone can verify the signature with your public key, which shows that the message came from you and has not been altered.
- **Key exchange**: two parties combine their own private key with the other's public key to arrive at the same shared secret, which can then be used to derive a symmetric key.

The weak point is trust in the public key itself. If Alice wants to send a message to Bob and she has a public key that claims to be Bob's, nothing in the mathematics tells her whether it really belongs to Bob or to an attacker in the middle of the conversation. **Public Key Infrastructure** (PKI) addresses this: a **Certification Authority** (CA), a trusted third party, checks an identity and signs a **certificate** binding that identity to a public key. The section on [chains of trust](#chains-of-trust) shows how this works in practice.

## How RSA works {#how-rsa-works}

RSA needs only a little maths, and every term is explained here the first time it appears:

- A **prime** number can only be divided exactly by 1 and by itself: 2, 3, 5, 7, 11, 13 and so on. 15 is not prime, because 15 = 3 × 5.
- An **exponent** is a power: in 5³ (written `5**3` in Python) the exponent is 3, and 5³ = 5 × 5 × 5 = 125.
- **mod** gives the remainder after a division: 17 mod 5 = 2, because 17 ÷ 5 = 3 remainder 2. The number you divide by (5 here) is called the **modulus**. Python writes it as `%`, so `17 % 5` is 2.

RSA uses two exponents. The **encryption exponent** *e* is part of the public key, and the **decryption exponent** *d* is part of the private key. To encrypt, you raise the message to the power *e*; to decrypt, you raise the result to the power *d*; both times you keep only the remainder mod a large number *N*. The cleverness of RSA is in choosing *d* so that it exactly undoes *e*, which you can only do if you know the secret primes that *N* was made from. The steps are:

1. Choose two large prime numbers, *p* and *q*, and keep them secret.

2. Compute the **modulus** *N*, which is part of both keys:

    N = p × q

3. Compute **φ(N)** (Euler's totient, written phi(N) or phiN in code):

    φ(N) = (p − 1)(q − 1)

4. Choose the encryption exponent *e*, which must satisfy two conditions:

    - 1 < e < φ(N)
    - e is **coprime** with φ(N), which means their greatest common divisor is 1, so they share no factors (written: gcd(e, φ(N)) = 1)

5. Compute the decryption exponent *d*. In words: *d* is the number that, multiplied by *e*, leaves a remainder of 1 when divided by φ(N). This is called the **modular inverse** of *e*:

    (e × d) mod φ(N) = 1, which is written d = e⁻¹ mod φ(N)

    Python works it out for you with `pow(e, -1, phiN)` (using a method called the extended Euclidean algorithm).

The **public key** is the pair (e, N) and the **private key** is (d, N). A message *M* (a number smaller than *N*) is encrypted and decrypted as follows:

- Encryption: C = M^e mod N
- Decryption: M = C^d mod N

RSA's security rests on the **RSA problem**: given only *N* and *e*, recovering *M* from *C* is believed to be as hard as factoring *N* back into *p* and *q*. Anyone who knows *p* and *q* can compute φ(N), and from it *d*, which is why the primes must stay secret and must be large (real keys use primes of 1024 bits or more each).

## RSA by hand, with Python {#rsa-by-hand-with-python}

Let's work through an example of the RSA algorithm using small numbers. In practice these values would be hundreds of digits long, but the steps are the same:

==action: Open the Python REPL[^2]:==

[^2]: **R**ead **E**valuate **P**rint **L**oop, an interactive programming environment.

```bash
python3
```

==action: Pick two primes and compute N and φ(N):==

```python
p = 7
q = 23
N = p * q                  # the modulus
phiN = (p - 1) * (q - 1)   # phi(N)
print(N, phiN)
```

This gives *N* = 161 and φ(N) = 132.

==action: List the values that could be used for e:==

```python
import math
# every number between 2 and phiN that shares no factors with phiN
[e for e in range(2, phiN) if math.gcd(e, phiN) == 1]
```

The list starts 5, 7, 13, 17, 19, ... Even numbers never appear, because φ(N) is always even; neither do multiples of 3 or 11, which 132 also cleanly divides by. This example uses e = 5, so the public key is (5, 161).

==action: Compute d and check it:==

```python
e = 5
d = pow(e, -1, phiN)       # the modular inverse of e
print(d, (e * d) % phiN)   # (e x d) mod phiN should be 1
```

This gives d = 53, and (5 × 53) mod 132 = 1 as required, so the private key is (53, 161).

==action: Encrypt and decrypt a number:==

```python
M = 67              # the message: a number smaller than N
C = pow(M, e, N)    # encrypt with the public key: C = M^e mod N
print(C)
M = pow(C, d, N)    # decrypt with the private key: M = C^d mod N
print(M)
```

`pow(M, e, N)` computes M^e mod N without first computing the enormous number M^e, which is how real RSA implementations handle numbers of thousands of bits. Here the ciphertext is 37, and decrypting it gives 67 again.

### Encrypting text {#encrypting-text}

RSA works on numbers, so text has to be turned into numbers first. Python's built-in `ord()` function takes a character and returns the number that represents it in [ASCII](https://www.rapidtables.com/code/text/ascii-table.html), and `chr()` does the reverse. ==action: Try them:==

```python
ord('T')
ord('h')
chr(84)
```

`ord('T')` is 84, `ord('h')` is 104, and `chr(84)` turns 84 back into `'T'`. ==action: Encrypt the first two characters of "The Hobbit" by hand:==

```python
pow(84, e, N)     # 'T' encrypted
pow(104, e, N)    # 'h' encrypted
```

They encrypt to 7 and 41. ==action: Decrypt 7 and turn it back into a character:==

```python
pow(7, d, N)          # gives 84 again
chr(pow(7, d, N))     # gives 'T'
```

Instead of doing this for each character one by one, a `for` loop can do every character of the message in turn. ==action: Encrypt the whole message:==

```python
message = "The Hobbit"
ciphertext = []
for character in message:
    number = ord(character)          # the character as a number
    encrypted = pow(number, e, N)    # encrypt that number
    ciphertext.append(encrypted)     # add it to the list

print(ciphertext)
```

> Note: In the REPL, press Enter on an empty line to finish the `for` loop before the `print` line.

The ciphertext is `[7, 41, 54, 100, 151, 34, 140, 140, 119, 93]`. ==action: Decrypt it the same way:==

```python
decrypted = ""
for number in ciphertext:
    character = chr(pow(number, d, N))   # decrypt, then turn back into a character
    decrypted = decrypted + character

print(decrypted)
```

This prints `The Hobbit` again. Python can also write the same decryption on one line, which you may see in other people's code:

```python
''.join(chr(pow(number, d, N)) for number in ciphertext)
```

> Question: Look at the two 140s in the ciphertext. Which letters are they, and what could an attacker learn from repeated numbers in a longer message?

Encrypting one character at a time in this way (known as **textbook RSA**) always turns the same character into the same number. Counting how often each encrypted number appears, and matching the most common ones to the most common letters in English (e, t, a and so on), is known as **frequency analysis**, and it breaks this kind of encryption without ever needing the key. To make that analysis impossible, modern RSA implementations encrypt whole blocks of data at a time and add random padding, so that encrypting the same message twice gives different ciphertexts.

==action: Leave the REPL open==; you will need it for the next five challenges. Hackerbot gives you different numbers from the example, so use the code above with Hackerbot's values.

#### Hackerbot Attack #1 {#hackerbot-attack-1}

You can skip the bot to here, by saying **goto 1**.

> Hackerbot: Given p and q, pick out the valid values for e from a list of candidates.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *the valid values, smallest first, separated by spaces*"==.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #2 {#hackerbot-attack-2}

You can skip the bot to here, by saying **goto 2**.

> Hackerbot: Given p, q and e, work out the decryption exponent d.

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

> Hint: Set d and N to Hackerbot's values, type Hackerbot's numbers into a list with commas between them (for example `ciphertext = [503, 1723, 95]`), then run the decryption loop from the worked example.

Don't forget to ==action: save and submit any flags!==

## RSA with OpenSSL {#rsa-with-openssl}

OpenSSL is the toolkit behind much of the encryption on the internet, and its `openssl` command gives you real RSA, with real key sizes and proper padding. ==action: Leave Python== (`exit()`) and ==action: make a working directory:==

```bash
mkdir -p ~/rsa_example
cd ~/rsa_example
```

==action: Generate a 2048-bit RSA private key, then export its public key:==

```bash
# generate Bob's private key: a 2048-bit RSA key (this contains p, q, N, e and d)
openssl genrsa -out bob.priv 2048
# export the public half (N and e only) into a separate file Bob can give to anyone
openssl rsa -in bob.priv -pubout -out bob.pub
cat bob.priv
cat bob.pub
```

The private key file starts with `-----BEGIN PRIVATE KEY-----` and the public key file starts with `-----BEGIN PUBLIC KEY-----`. Both are PEM files: binary data encoded as base64, between header lines.

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
echo 'Meet me at Tiki Cafe at 3' > msg.txt
openssl pkeyutl -encrypt -pubin -inkey bob.pub -in msg.txt -out msg.enc
openssl pkeyutl -decrypt -inkey bob.priv -in msg.enc
```

> Note: `-pubin` tells OpenSSL that the key file is a public key. Encrypt the same file twice and compare the two outputs (`cmp msg.enc msg2.enc`): they differ, because OpenSSL adds random padding, which is what textbook RSA lacks.

#### Hackerbot Attack #6 {#hackerbot-attack-6}

You can skip the bot to here, by saying **goto 6**.

> Hackerbot: Create a 2048-bit RSA key pair, with the file names Hackerbot gives you, in the directory Hackerbot names.

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

==action: Sign a file with Bob's private key:==

```bash
cd ~/rsa_example
echo 'I owe Alice 10 pounds' > iou.txt
# hash iou.txt with SHA-256 and sign the hash with Bob's private key; the signature goes in iou.sig
openssl dgst -sha256 -sign bob.priv -out iou.sig iou.txt
# show the signature as hex, and its size in bytes
xxd -p iou.sig
wc -c iou.sig
```

The signature is 256 bytes (2048 bits, the size of Bob's key), however long or short the message is. It is not the message, or even an encrypted copy of it: it is a value that only Bob's private key could have produced from this exact file. ==action: Verify it with Bob's public key:==

```bash
# recompute the hash of iou.txt and check it against the signature, using Bob's public key
openssl dgst -sha256 -verify bob.pub -signature iou.sig iou.txt
```

This prints `Verified OK`. ==action: Now change the message and verify it again:==

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

## Key exchange {#key-exchange}

Public-key encryption can deliver a symmetric key, but modern protocols normally use **key exchange** instead: both parties contribute to the key, and it is never sent at all. **Diffie-Hellman** (DH), published in 1976, was the first public-key scheme of any kind.

1. Alice and Bob agree, in public, on a prime *p* and a **generator** *g* (a number whose powers mod *p* produce every value from 1 to *p* − 1).
2. Alice picks a private value *a* and sends Bob A = g^a mod p.
3. Bob picks a private value *b* and sends Alice B = g^b mod p.
4. Alice computes B^a mod p and Bob computes A^b mod p. Both equal g^(ab) mod p, which is their shared secret.

An eavesdropper sees *p*, *g*, *A* and *B*, but recovering *a* or *b* from them (the **discrete logarithm problem**) is infeasible for large numbers.

==action: Work through an example in Python:==

```python
p, g = 23, 5         # public: agreed in the open
a, b = 6, 15         # private: Alice's and Bob's secret values
A = pow(g, a, p)     # Alice sends A to Bob
B = pow(g, b, p)     # Bob sends B to Alice
print(A, B, pow(B, a, p), pow(A, b, p))
```

Alice sends 8, Bob sends 19, and both arrive at the shared secret 2.

#### Hackerbot Attack #9 {#hackerbot-attack-9}

You can skip the bot to here, by saying **goto 9**.

> Hackerbot: Hackerbot gives you p, g, your private value and its own public value; work out your public value and the shared secret.

When you are ready for the bot to ask the question, ==action: say 'ready'== to Hackerbot.

There is a quiz to complete: ==action: answer with "answer *your public value A* *the shared secret*"== (two numbers separated by a space).

Don't forget to ==action: save and submit any flags!==

### Elliptic-curve key exchange and forward secrecy {#x25519}

Today's protocols mostly use **elliptic-curve Diffie-Hellman**, in particular **X25519**, which follows the same steps on an elliptic curve with much shorter keys. TLS 1.3 uses a fresh key exchange for every connection, which gives **forward secrecy**: even if a server's long-term private key is stolen later, recorded traffic cannot be decrypted, because the keys that protected it no longer exist.

Key exchange leaves one problem open. Diffie-Hellman lets you agree a secret with *someone*, but on its own it does not tell you *who*: an attacker in the middle of the connection can run one exchange with you and another with your bank, and pass messages between the two, reading everything. The server has to prove who it is, which is what certificates are for.

## Chains of trust {#chains-of-trust}

A **certificate** is a public key plus an identity (such as a website's domain name), signed by a CA. During the key exchange, the website signs its part of the exchange with the private key that belongs to its certificate, so your browser knows the key exchange really is with the bank and not with someone in the middle.

Your browser trusts a few hundred **root CAs**, whose self-signed certificates ship with the browser or the operating system in a **root store**. Roots are too valuable to use every day, so their private keys are kept offline, and a root instead signs a small number of **intermediate CAs**, which sign the certificates for websites (**leaf** certificates):

```
Root CA              in your browser's root store (it signs itself)
  └─ signs ─▶ Intermediate CA
                └─ signs ─▶ www.example.com   the website's (leaf) certificate
```

When you visit a website, it sends its leaf certificate and the intermediate; your browser checks each signature up the chain until it reaches a root it already trusts. So whoever holds an intermediate CA's private key can issue a certificate for any website, and every browser that trusts the root will accept it.

## When a CA gets key generation wrong {#when-a-ca-gets-key-generation-wrong}

In October 2026 the security researcher Scott Helme found that the private keys of eight intermediate CAs run by eMudhra, an Indian CA trusted by every major browser, could be recovered from the CAs' public certificates in under a second. The keys were P-384 **elliptic-curve** keys, and a private key of that kind should be a random 384-bit number, one of roughly 10^115 possibilities; every one of eMudhra's eight fitted in a single byte, and one of them was the number 4. The keys came from a key-generation ceremony in April 2025 that produced almost no randomness, and they had sat in public certificates for 17 months before anyone noticed.

Anyone holding one of those keys could have signed a certificate for any website (a bank, an email provider, a government site), and every browser would have accepted it. The mathematics of elliptic-curve cryptography was sound throughout; the failure lay entirely in how the keys were generated.

### What a key of 4 looks like {#what-a-key-of-4-looks-like}

An elliptic-curve private key is a number *k*, and the public key is the point *k* × *G*, where *G* is a fixed point on the curve (P-384 here) that everyone knows. ==action: Make a key with eMudhra's private key of 4, using Python's pycryptodome library, and save it:==

```bash
mkdir -p ~/emudhra_example
cd ~/emudhra_example
python3
```

```python
from Cryptodome.PublicKey import ECC
weak = ECC.construct(curve='P-384', d=4)     # a P-384 key whose private value is 4
open('emudhra4.pem', 'w').write(weak.export_key(format='PEM'))
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

Recovering *k* from the public key is the elliptic-curve equivalent of factoring, and is infeasible when *k* is a random 384-bit number. When *k* is small, you can simply try every value in turn: compute 1 × *G*, 2 × *G*, 3 × *G*, and so on, until one matches the public key.

#### Hackerbot Attack #10 {#hackerbot-attack-10}

You can skip the bot to here, by saying **goto 10**.

> Hackerbot: Hackerbot leaves you a root CA certificate and an intermediate CA certificate whose private key is one of eMudhra's; brute-force it.

==action: Say 'ready'== to Hackerbot, which puts the two certificates in ~/emudhra_ca. ==action: Look at the intermediate, and extract its public key:==

```bash
cd ~/emudhra_ca
openssl x509 -in lab_trust_inter.crt -noout -text | head -20
openssl x509 -in lab_trust_inter.crt -noout -pubkey > inter_pub.pem
python3
```

==action: Try every private key in turn until one matches:==

```python
from Cryptodome.PublicKey import ECC
pub = ECC.import_key(open('inter_pub.pem').read())
for k in range(1, 100000):
    guess = ECC.construct(curve='P-384', d=k)     # the key whose private value is k
    if guess.pointQ == pub.pointQ:                # does its public key match?
        print('Found the private key:', k)
        break

```

> Note: Press Enter on the blank line to run the loop. It should take no more than a second or two.

There is a quiz to complete: ==action: answer with "answer *the private key*"==.

Don't forget to ==action: save and submit any flags!==

### Forging a certificate {#forging-a-certificate}

With the intermediate's private key you can now do what an attacker could have done with eMudhra's: issue a certificate for a website you don't own, which chains up to a trusted root.

#### Hackerbot Attack #11 {#hackerbot-attack-11}

You can skip the bot to here, by saying **goto 11**. This is the final challenge.

> Hackerbot: Forge a certificate for the bank website Hackerbot names, signed with the intermediate key you recovered.

==action: Save the key you recovered as a PEM file.== Still in Python (replace `k` with your number if you have left the loop):

```python
key = ECC.construct(curve='P-384', d=k)
open('recovered.pem', 'w').write(key.export_key(format='PEM'))
exit()
```

==action: Make a key and a certificate request for the bank's website, then sign it with the stolen intermediate key:==

```bash
cd ~/emudhra_ca
# type in the bank website name that Hackerbot gives you
read -p "Bank website: " BANK
# the "bank's" key and certificate signing request
openssl genrsa -out fake.key 2048
openssl req -new -key fake.key -subj "/CN=$BANK" -out fake.csr
# sign it as the intermediate CA, with the private key you brute-forced
openssl x509 -req -in fake.csr -CA lab_trust_inter.crt -CAkey recovered.pem -CAcreateserial -days 30 -out fake.crt
# check it the way a browser would: up the chain to the trusted root
openssl verify -CAfile lab_trust_root.crt -untrusted lab_trust_inter.crt fake.crt
```

`fake.crt: OK`: any client that trusted that root would accept your certificate for the bank, and an attacker in the middle of a connection could use it to pose as the bank during the key exchange.

When you are ready for the bot to check your certificate, ==action: say 'ready'== to Hackerbot.

Don't forget to ==action: save and submit any flags!==

## The response {#the-response}

eMudhra revoked the eight intermediate certificates the day after the report, but revocation only works if clients check, and in practice many don't: checking slows connections down and tells the CA which sites you visit, and most clients carry on if the check fails (which an attacker in the middle can easily arrange). So the browsers acted directly. Chrome pushed out a blocklist of the eight compromised keys to every browser within two hours of the report, Mozilla, Apple and Microsoft followed with their own updates, and Chrome later removed eMudhra's root from its root store entirely, so that nothing it had signed would be trusted. Within days eMudhra announced that it was leaving the public TLS certificate business. See references [6] to [8] for more detail.

> Question: Revoking a certificate only helps if clients check for revocation, and many programs never do. Why did the browsers block eMudhra's keys, and later remove its root, rather than rely on revocation?

The lasting fix is a key that nobody can guess. CAs are meant to generate their keys inside a **hardware security module** (HSM), a tamper-resistant device that creates and stores keys and never releases them, during a witnessed and recorded key ceremony, from a cryptographically secure random number generator. eMudhra's failure shows that the result still needs checking: a single look at the private key values would have caught it. Certificate lifetimes are also being cut (to 47 days by 2029), which limits how long any mis-issued certificate stays useful.

## How secure is RSA today? (optional reading) {#how-secure-is-rsa-today}

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
