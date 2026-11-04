---
title: "Exfiltration Detection: Data Loss Prevention with Snort"
author: ["Z. Cliffe Schreuders"]
license: "CC BY-SA 4.0"
description: "Configure Snort to detect the exfiltration of sensitive data over the network, using text, regular expression and hash-based matching, then investigate and contain a live incident while fending off Hackerbot's attacks."
overview: |
  In this lab you will use Snort, an intrusion detection system (IDS), to spot sensitive data being exfiltrated over the network, which is one part of data loss prevention (DLP). Data breaches and insider threats remain a serious risk to organisations, and watching for sensitive data as it leaves the network is one way to catch them.

  Hackerbot will break into a web server and steal a client list containing credit card details and national insurance numbers. You will write Snort rules that detect the theft: first by matching the data directly, then with regular expressions that match the format of the records without containing any of them, and finally with hash-based matching, so that the rules themselves hold no sensitive data. Along the way you will track down a copy of the data left at rest on a server. Finally, you will respond to an incident: capture and investigate a fresh theft to work out what was taken, where it went and how the attacker got in, then contain the breach with a packet filter without taking the website offline.

  This is a Hackerbot lab. Work through the lab sheet, then when prompted interact with Hackerbot.
tags: ["dlp", "snort", "ids", "ips", "exfiltration-detection", "network-monitoring", "regular-expressions", "hashing", "incident-response", "iptables", "mitre-attack", "hackerbot"]
categories: ["response_and_investigation"]
type: ["ctf-lab", "hackerbot-lab", "lab-sheet"]
difficulty: "intermediate"
cybok:
  - ka: "F"
    topic: "Artifact Analysis"
    keywords: ["cryptographic hashing"]
  - ka: "SOIM"
    topic: "Monitor: Data Sources"
    keywords: ["network traffic"]
  - ka: "SOIM"
    topic: "Analyse: Analysis Methods"
    keywords: ["exfiltration detection / data loss prevention"]
  - ka: "NS"
    topic: "Network Defence Tools"
    keywords: ["packet filters", "intrusion detection systems", "intrusion prevention systems", "IDS rules creation"]
---

## Getting started {#getting-started}

\==action: Start these VMs== (if you haven't already):

- hackerbot_server (leave it running, you don't log into this)
- ids_monitor
- web_server (leave it running, you don't log into this through the GUI console; you will SSH into it later in the lab)
- desktop

All of these VMs need to be running to complete the lab.

\==action: Note the IP address of the web_server VM== from the VM list you were given when you claimed the VMs, or by running `ip a` on it. You will need it to test your rules and, later, to log in to it over SSH.

### Your login details for the "desktop" and "ids_monitor" VMs {#your-login-details}

\==VM: On the desktop and ids_monitor VMs==, log in using:

- User: ==edit: your username, given to you when you claimed the VMs==
- Password: `tiaspbiqe2r` (**t**his **i**s **a** **s**ecure **p**assword **b**ut **i**s **q**uite **e**asy **2** **r**emember)

The same username and password also work over SSH on the web_server VM. You won't log in to the hackerbot_server or web_server through the GUI console, but all the VMs need to be running to complete the lab.

{% include hackerbot-intro.md role="task you with monitoring the network, and who will attack your systems" %}

## Getting Snort up and running {#getting-snort-up-and-running}

\==VM: On the ids_monitor VM:==

\==action: Confirm Snort's output== is set to something readable:

```bash
sudo vi /etc/snort/snort.conf
```

> Note: Editing using vi involves pressing "i" to insert/edit text, then *Esc*, ":wq" to write changes and quit.

\==action: Check for the following lines:==

```
output alert_fast
output log_tcpdump: tcpdump.log
```

```bash
sudo vi /etc/snort/snort.debian.conf
```

\==action: Check Snort's interface==, which should be ens19, and that the local network is set to your IP address range (or "any"):

> Note: Confirm the above, then exit vi (*Esc*, ":wq").

\==action: Restart Snort:==

```bash
sudo service snort stop
sudo service snort start
```

> Note: Using "reload" or "restart" may not update the interface.

Snort should now be running, monitoring network traffic for activity. Every VM in this lab forwards a copy of the traffic it receives to the ids_monitor VM, so Snort sees the traffic between all of them.

It can be helpful to monitor network traffic while writing IDS rules. You can start Wireshark with:

```bash
sudo wireshark &
```

Remember, you should add your rules to `/etc/snort/rules/local.rules`.

> Tip: If Snort stops when you restart it, one of your rules probably has a syntax error. Run `sudo snort -T -c /etc/snort/snort.conf` to check your configuration and rules; the error message names the line at fault.

---

## Data loss prevention (DLP) {#data-loss-prevention-dlp}

Data loss prevention (DLP) involves monitoring for activity that indicates that sensitive information is being exfiltrated or handled incorrectly. Some DLP systems monitor local systems and data at rest (for example, host-based IDS), whilst others focus on network traffic and data in motion (network-based IDS). DLP can help to detect insecure processes in an organisation, such as sensitive data being stored in unplanned or insecure places, and it can help to mitigate both insider threats and exfiltration by remote attackers.

Some of the most damaging leaks come from insiders. In 2014 a senior internal auditor at the supermarket Morrisons copied the payroll data of around 100,000 colleagues, including names, addresses, bank details and national insurance numbers, and published it online; the resulting case reached the UK Supreme Court in 2020 ([WM Morrisons Supermarkets plc v Various Claimants [2020] UKSC 12](https://supremecourt.uk/cases/uksc-2018-0213.html)). Data can also leave an organisation by accident, through new channels: in 2023 Samsung engineers pasted confidential source code and meeting notes into ChatGPT, after which the company restricted staff use of generative AI tools ([The Register, 2023](https://www.theregister.com/2023/05/02/samsung_generative_ai_ban/)). Breaches are also expensive: IBM's *Cost of a Data Breach Report 2025* puts the global average cost of a breach at US$4.44 million ([IBM, 2025](https://www.ibm.com/reports/data-breach)).

There is a variety of DLP solutions available. Enterprise solutions typically combine network monitoring (data in motion), file system monitoring (data at rest), and monitoring of local transfers (for example, copying files to USB storage) to block exfiltration using local storage devices. In order to be effective, an organisation must first identify the sensitive data that should be monitored.

### Snort exfiltration detection (data in motion) {#snort-exfiltration-detection-data-in-motion}

#### Testing your rules {#testing-your-rules}

Each Hackerbot attack takes a few minutes, so it is worth testing your rules yourself first. The web_server VM runs an FTP server, so you can fetch a copy of a file from it in the clear and check that Snort raises your alert.

\==VM: On the ids_monitor VM==, \==action: watch the alerts as they arrive:==

```bash
sudo tail -f /var/log/snort/alert
```

\==VM: On the desktop VM==, \==action: fetch the client list from the web_server over FTP:==

```bash
curl --noproxy '*' ftp://==edit: your username==:tiaspbiqe2r@==edit: web_server IP address==/trade_secrets/clients.csv
```

Your alert should appear on the ids_monitor VM. Change `clients.csv` to `fake_clients.csv` to test against the other file. (The `--noproxy` option stops curl trying to send the request through the lab's web proxy, which doesn't handle FTP.)

> Note: Remember to restart Snort (stop, then start) after you edit your rules.

#### Text-based exfiltration detection {#text-based-exfiltration-detection}

> Tip: You can include your sensitive data directly in a Snort rule. This is closely related to the [IDS rules lab](https://cliffe.github.io/HacktivityLabSheets/labs/response_and_investigation/5-ids-rules/), which will be a helpful resource.

The attacker won't use a standard port, so match traffic on any port (`any any -> any any`); it is fine to monitor all ports, so long as your rule(s) detect transfer of the file. Adding `flow:established;` limits the rule to established TCP connections, which cuts out some noise.

#### Regular expression-based exfiltration detection {#regular-expression-based-exfiltration-detection}

Assuming the data you are protecting is sensitive, you likely don't want your Snort rules to contain direct copies of all your most sensitive data: anyone who can read your IDS configuration could read the data. For this reason, Snort rules can contain regular expressions to match against.

It is possible to write Snort rules that detect the transfer of the contents of your files based on the format of the data (what a card number or a national insurance number looks like), so that the Snort rule contains none of the sensitive values themselves.

> Tip: Consider using the `pcre` keyword in your rule.

Next to the real client list on the web_server there is a second file, `fake_clients.csv`, filled with made-up records in the same format. A fake record like this is a *honeytoken* (or canary record): data that nobody has any legitimate reason to touch, so any sign of it on the network, or anywhere else, indicates a leak. Commercial DLP and deception products plant honeytokens for the same reason. A rule that matches the format of the records catches the real data and the honeytoken alike.

#### Hash-based exfiltration detection {#hash-based-exfiltration-detection}

Snort rules can also contain hashes to match against, so that the rule doesn't contain any plain text of your document at all.

Using newer versions of Snort (2.9.7 onwards) it is possible to write Snort rules that detect the transfer of the contents of your files based on hashes, using the `protected_content` keyword.

#### Hackerbot Attack #1 {#hackerbot-attack-1}

You can skip the bot to here, by saying **goto 1**.

> Hackerbot: Write one or more Snort rules that detect the sensitive client list being transferred unencrypted, using text-based matching. Hackerbot gives you the file to protect and the alert message to use in the chat.

Do any necessary preparation, then when you are ready for the bot to complete the attack, ==action: say 'ready'==.

> Note: Add your rule to `/etc/snort/rules/local.rules` on the ids_monitor VM, using the exact alert message Hackerbot gave you in the chat.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #2 {#hackerbot-attack-2}

You can skip the bot to here, by saying **goto 2**.

> Hackerbot: Update your rule to use a regular expression, so that it matches the data without containing any of it. Hackerbot will steal either the real client list or the fake one, so your rule needs to match both.

Do any necessary preparation, then when you are ready for the bot to complete the attack, ==action: say 'ready'==.

There is a quiz to complete: once Hackerbot asks you the question, you can ==action: answer '*YOURANSWER*'==.

> Note: The question asks where else on the web_server VM Hackerbot's attacks have left a copy of the real client list. Search for it using a regular expression (don't use hashes), and answer with the file's full path.

\==action: Log in to the web_server VM over SSH== from the desktop VM to search it:

```bash
ssh ==edit: your username==@==edit: web_server IP address==
```

> Hint: The command `grep -r` with the `-E` option searches files for an extended regular expression; `-l` lists only the names of the matching files. Think about which directories a process that isn't root could write to.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attack #3 {#hackerbot-attack-3}

You can skip the bot to here, by saying **goto 3**.

> Hackerbot: Update your rule again to use hash-based matching, so that the rule holds no sensitive data. Hackerbot gives you the file and the alert message in the chat.

Do any necessary preparation, then when you are ready for the bot to complete the attack, ==action: say 'ready'==.

> Hint: You don't know where in the packet the protected content will be, so you can first have a `content` match on something you know will come first (such as the start of the first record), and specify a `distance` between that and the `protected_content`. A name is fine as an anchor, but don't include card numbers, national insurance numbers, phone numbers or email addresses in your rule.

Don't forget to ==action: save and submit any flags!==

---

## Incident response {#incident-response}

Detection is only the start. Once an alert fires, an incident response team has to work out what happened (how the attacker got in, what was taken and where it went), contain the incident to stop further damage, and meet its legal obligations, such as reporting a personal data breach. The next attacks take you through these steps.

### Capturing and investigating the incident {#capturing-and-investigating}

Snort's `log_tcpdump` output only keeps the packets that triggered an alert, so to see the whole incident you need your own capture of the traffic.

\==VM: On the ids_monitor VM==, \==action: start a packet capture== before Hackerbot attacks, either in Wireshark (capturing on ens19) or with tcpdump:

```bash
sudo tcpdump -i ens19 -w ~/incident.pcap
```

Leave it running until the attack has finished, then stop it with *Ctrl-C*, and open the capture in Wireshark:

```bash
sudo wireshark ~/incident.pcap &
```

> Tip: In Wireshark, *Statistics → Conversations* lists who talked to whom on which ports, and right-clicking a packet and choosing *Follow → TCP Stream* shows the data a connection carried. Your Snort alerts from the earlier attacks will also fire and show you where to look.

The attacker in this lab uses a common technique: the data is sent back over the same connection the attacker uses to control the compromised server (its command and control, or C2, channel). [MITRE ATT&CK](https://attack.mitre.org/) is a knowledge base of attacker tactics and techniques, used across the industry to describe and share what attackers do; the techniques for getting data out are listed under the [Exfiltration tactic](https://attack.mitre.org/tactics/TA0010/).

The stolen records contain personal data, including national insurance numbers, so this is a personal data breach. Under UK GDPR (Article 33), an organisation must report a personal data breach to the Information Commissioner's Office (ICO) without undue delay, and within a fixed number of hours of becoming aware of it, unless the breach is unlikely to result in a risk to people's rights and freedoms; see the [ICO's guidance on reporting a breach](https://ico.org.uk/for-organisations/report-a-breach/). Card data brings further obligations under the payment card industry's data security standard (PCI DSS).

#### Hackerbot Attack #4 {#hackerbot-attack-4}

You can skip the bot to here, by saying **goto 4**.

> Hackerbot: Hackerbot breaks in and steals some of the client records again, this time sending them somewhere less predictable. Capture the traffic, then work out where the data went.

\==action: Start your capture==, then ==action: say 'ready'==. Once the attack has finished, ==action: answer '*YOURANSWER*'== to Hackerbot's question.

Don't forget to ==action: save and submit any flags!==

#### Hackerbot Attacks #5 to #8 {#hackerbot-attacks-5-to-8}

You can skip the bot to these, by saying **goto 5** (and so on).

> Hackerbot: Hackerbot asks you a series of questions about the incident: what was taken, how the attacker got in, which ATT&CK technique describes the exfiltration, and how quickly the breach must be reported.

Each of these is a question only: Hackerbot asks it as soon as you get there, so investigate your capture and ==action: answer '*YOURANSWER*'== (say 'ready' to hear the question again).

> Hint: For the service the attacker exploited, look at the connections to the web_server VM from just before the data left, and the port they used.

Don't forget to ==action: save and submit any flags!==

---

## Intrusion prevention systems (IPS) {#intrusion-prevention-systems}

An IDS detects and reports; an intrusion prevention system (IPS) can also block. The difference comes down to where the system sits. A *passive* IDS, as in this lab, receives a copy of the traffic: here, each VM forwards a copy of every packet it receives to the ids_monitor VM (using an iptables `TEE` rule), so Snort can raise an alert, but by the time it does so the original packet has already been delivered. An *inline* IPS sits in the path of the traffic, so every packet passes through it on its way, and the IPS decides whether to forward it or drop it.

Snort can run inline as an IPS (for example, between two network interfaces), and its rules then support actions that block traffic as well as report it:

| Action | Effect |
|---|---|
| `alert` | Generate an alert and log the packet |
| `log` | Log the packet without an alert |
| `pass` | Ignore the packet |
| `drop` | Block and log the packet |
| `reject` | Block the packet, log it, and send a TCP reset or ICMP unreachable to the sender |
| `sdrop` | Block the packet without logging it |

For example, the following rule, running inline, would stop any packet containing a particular honeytoken value from leaving:

```
drop tcp $HOME_NET any -> any any (msg:"Honeytoken leaving the network"; content:"QQ000000A"; sid:1000100; rev:1;)
```

Blocking has costs that alerting does not. A false positive in an IDS wastes an analyst's time; a false positive in an IPS blocks legitimate traffic, potentially taking a business service offline. An inline device also adds latency, and has to be designed to either let traffic through (fail open) or block everything (fail closed) if it stops working. For these reasons organisations often run new rules in alert-only mode first, and only switch them to `drop` once they are confident the rules don't match legitimate traffic.

## Containment with a packet filter {#containment-with-a-packet-filter}

This lab's IDS can't block anything, so to contain the breach you will use a packet filter on the web_server VM itself. A packet filter, such as Linux's iptables, is a firewall that decides whether to accept or drop each packet based on its headers (source and destination addresses, protocol, ports and connection state), without inspecting its contents the way Snort does.

\==VM: On the web_server VM== (over SSH, as above), these commands show and change the rules:

```bash
sudo iptables -L -n -v --line-numbers
```

```bash
sudo iptables -A INPUT -p tcp --dport ==edit: port== -j DROP
```

```bash
sudo iptables -D INPUT ==edit: rule number==
```

The first lists the current rules with their numbers, the second adds a rule dropping incoming TCP traffic to a port, and the third deletes a rule by its number. Rules for traffic leaving the VM go in the `OUTPUT` chain, where `--dport` is the port being connected to, and `-d` and `-s` match the destination and source addresses.

> Warning: Keep SSH (port 22) and the website (port 80) open: Hackerbot uses SSH to check your work, and containment shouldn't take the business offline. If you lock yourself out, restarting the web_server VM clears rules that you haven't saved.

> Note: Your rules also block the earlier attacks. To repeat any of them, delete your rules first.

#### Hackerbot Attack #9 {#hackerbot-attack-9}

You can skip the bot to here, by saying **goto 9**.

> Hackerbot: Use a packet filter on the web_server VM to stop Hackerbot getting data out, without taking the website offline. Hackerbot attacks again, then checks that the website still works.

Do any necessary preparation, then when you are ready for the bot to complete the attack, ==action: say 'ready'==. If your filter works, Hackerbot asks one more question, about intrusion prevention: ==action: answer '*YOURANSWER*'==.

Don't forget to ==action: save and submit any flags!==

## Resources {#resources}

The Snort Project (n.d.) *Snort Users Manual 2.9.16*, chapter 3: Writing Snort Rules. Available from: <https://www.snort.org/documents/snort-users-manual-2-9-16-html>

MITRE (n.d.) *ATT&CK: Exfiltration (TA0010)*. Available from: <https://attack.mitre.org/tactics/TA0010/>

Information Commissioner's Office (n.d.) *Report a breach*. Available from: <https://ico.org.uk/for-organisations/report-a-breach/>

IBM (2025) *Cost of a Data Breach Report 2025*. Available from: <https://www.ibm.com/reports/data-breach>
