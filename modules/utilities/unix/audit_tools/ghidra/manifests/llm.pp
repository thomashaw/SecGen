# Optional LLM assistant: the GhidrAssist plugin, preconfigured with OpenAI-compatible providers (normally the local
# Spark LLMs, reached through an llm_relay VM). Kali only: GhidrAssist 2.x needs Ghidra 12.
class ghidra::llm {
  require ghidra::install
  Exec { path => ['/bin', '/usr/bin', '/usr/local/bin', '/sbin', '/usr/sbin'] }

  $secgen_parameters = secgen_functions::get_parameters($::base64_inputs_file)
  $llm_assistant = str2bool($secgen_parameters['llm_assistant'][0])

  if $llm_assistant {
    if $operatingsystem != 'Kali' {
      notice('ghidra: llm_assistant needs the Kali (Ghidra 12) install; GhidrAssist not installed')
    } else {
      $relay_ip = $secgen_parameters['llm_relay_ip'][0]
      if $relay_ip and $relay_ip != '' {
        $relay_port = $secgen_parameters['llm_relay_port'][0]
        $llm_url = "http://${relay_ip}:${relay_port}/api/v1/"
      } else {
        $llm_url = $secgen_parameters['llm_api_url'][0]
      }
      $llm_key        = $secgen_parameters['llm_api_key'][0]
      $llm_models     = $secgen_parameters['llm_models']
      $ghidra_version = $ghidra::install::ghidra_version
      $ghidra_dir     = $ghidra::install::ghidra_dir

      # GhidrAssist's 12.1 build (extension.properties version=12.1). Unzipping it into the installation's
      # Extensions dir skips the GUI installer's exact version check (12.1 vs 12.1.x); Ghidra loads it at startup.
      $ga_zip    = 'ghidra_12.1_PUBLIC_20260530_GhidrAssist.zip'
      $ga_sha256 = '02f888911730e4e07f55bac9475e8627b217f354e082b80a7d433b230797c547'
      $ga_url    = "https://github.com/symgraph/GhidrAssist/releases/download/2.2.0/${ga_zip}"
      $ga_path   = "/var/tmp/${ga_zip}"
      $checksum  = "echo '${ga_sha256}  ${ga_path}' | sha256sum -c -"
      $download  = "curl -fsSL -C - --retry 5 --retry-delay 15 --connect-timeout 60 --speed-limit 10240 --speed-time 300 -o ${ga_path} ${ga_url}"
      # same download/verify/resume handling as the Ghidra zip in ghidra::install
      exec { 'download and unpack GhidrAssist':
        command  => "if ! ${checksum} >/dev/null 2>&1; then ${download}; rc=\$?; if [ \$rc -eq 22 ]; then rm -f ${ga_path}; fi; [ \$rc -eq 0 ] || exit \$rc; ${checksum} || { rm -f ${ga_path}; exit 1; }; fi; unzip -q -o ${ga_path} -d ${ghidra_dir}/Ghidra/Extensions && rm -f ${ga_path}",
        creates  => "${ghidra_dir}/Ghidra/Extensions/GhidrAssist/lib/GhidrAssist.jar",
        provider => shell,
        timeout  => 0,
      }

      # GhidrAssist keeps its providers in Ghidra's per-user preferences (~/.config/ghidra/ghidra_<ver>_PUBLIC/
      # preferences). The script sets those keys for every existing user and /etc/skel (accounts created later).
      # Root-only: it holds the API key.
      file { '/usr/local/sbin/secgen-ghidra-llm-prefs':
        ensure  => file,
        content => template('ghidra/secgen-ghidra-llm-prefs.sh.erb'),
        mode    => '0700',
        owner   => 'root',
        group   => 'root',
      }
      exec { 'configure GhidrAssist providers':
        command => '/usr/local/sbin/secgen-ghidra-llm-prefs',
        unless  => '/usr/local/sbin/secgen-ghidra-llm-prefs --check',
        require => [File['/usr/local/sbin/secgen-ghidra-llm-prefs'], Exec['download and unpack GhidrAssist']],
      }
    }
  }
}
