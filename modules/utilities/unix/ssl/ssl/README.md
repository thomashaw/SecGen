# Puppet SSL Module

####Table of Contents

[![Build Status](https://travis-ci.org/fnerdwq/puppet-ssl.svg?branch=master)](https://travis-ci.org/fnerdwq/puppet-ssl)

1. [Overview](#overview)
2. [Module Description](#module-description)
3. [Setup](#setup)
    * [What Ssl affects](#what-ssl-affects)
    * [Setup requirements](#setup-requirements)
    * [Beginning with Ssl](#beginning-with-Ssl)
4. [Usage](#usage)
5. [Limitations](#limitations)
6. [TODOs](#todos)

##Overview

This small ssl module creates a self signed ssl certificate.

Written for Puppet >= 3.4.0.

##Module Description

See [Overview](#overview) for now.

##Setup

###What Ssl affects

* Creation of ssl certificates with openssl.

###Setup Requirements

Nothing.
	
###Beginning with Ssl	

Simply include it and you will get a simple self signed certificate for your $::fqdn in /etc/ssl.

##Usage

Just include the module by 

```puppet
include ssl
```

If you want to create more certificates or have special configurations to do, use the define:
```puppet
ssl::self_signed_certficate { $::fqdn:
  common_name      => $::fqdn,
  email_address    => 'root@example.de',
  country          => 'DE',
  organization     => 'Example GmbH',
  days             => 730,
  directory        => '/etc/ssl/web',
  subject_alt_name => "DNS:*.${::domain}, DNS:${::domain}",
}
```

##Limitations:

Debian and RedHat like systems.
Tested on:

* Debian 7
* Ubuntu 12.04
* Centos 6.x

Puppet Version >= 3.4.0, due to specific hiera usage.


##TODOs:

* Allow for shipped keys.
* Allow different key types (rsa, dsa, ecdsa).
* Allow for passwords.
* ... suggestions?
