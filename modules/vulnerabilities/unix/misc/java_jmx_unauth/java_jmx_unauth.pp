# Java process exposing an unauthenticated, no-TLS remote JMX management agent.
contain java_jmx_unauth::install
contain java_jmx_unauth::service
Class['java_jmx_unauth::install']
-> Class['java_jmx_unauth::service']
