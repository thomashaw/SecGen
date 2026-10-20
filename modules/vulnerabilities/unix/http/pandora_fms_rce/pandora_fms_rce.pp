contain pandora_fms_rce::install
contain pandora_fms_rce::config
contain pandora_fms_rce::service

Class['pandora_fms_rce::install']
-> Class['pandora_fms_rce::config']
-> Class['pandora_fms_rce::service']
