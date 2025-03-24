include labtainers::install
include labtainers::config

Class['labtainers::install'] -> Class['labtainers::config']
