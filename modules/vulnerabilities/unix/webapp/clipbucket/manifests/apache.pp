class clipbucket::apache {
  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $port = $secgen_parameters['port'][0]
  $docroot = '/var/www/clipbucket'


  package {['mplayer', 'mencoder', 'flvtool2', 'libogg-dev', 'libvorbis-dev', 'lame', 'php5', 'php5-gd' ]:
     ensure => installed,
  }


  # Apache mod_rewrite enabled
  class { '::apache':
    default_vhost => false,
    default_mods => ['rewrite', 'php'],
    overwrite_ports => false,
  }

  ::apache::vhost { 'www-clipbucket':
    port    => $port,
    docroot => $docroot,
  }

}


# Requirements

# ClipBucket has a few requirements which must be met before you are able to install and use it. In this section, these requirements are explained.
#
# Linux Server (some old distributions are not supported)
# Apache Web Server
# MySQL (version 4 +)
# PHP ( PHP 5 - 7 )
# PHP Configuration
# safe_mode = off
# max_execution_time = 1000 (recommended to prevent timeouts during video upload/conversion)
# session.gc_maxlifetime = 14000 (recommended to prevent session expires during video upload)
# open_basedir = (no value)
# output_buffering = on
# upload_max_filesize = 100M (recommended maximum video upload size in MB)
# post_max_size = 100M (recommended maximum video upload size in MB)
# GD library Enabled (v2 or higher)
# Mplayer + Mencoder (http://www.mplayerhq.hu/design7/dload.html)
# Flv2tool (http://inlet-media.de/flvtool2)
# Libogg + Libvorbis (http://www.xiph.org/downloads)
# LAME MP3 Encoder (http://lame.sourceforge.net)
# Apache mod_rewrite Enabled
# Must be able to run PHP from the command line (CLI) with exec()
# Allowed execution of background processes with exec("binary > /dev/null &")
