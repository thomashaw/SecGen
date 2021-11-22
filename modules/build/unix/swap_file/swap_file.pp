$secgen_params = secgen_functions::get_parameters($::base64_inputs_file)
$swapfile_size = $secgen_params['size'][0]
class { 'swap_file':
  files => {
    'swap1' => {
      ensure       => present,
      swapfile     => '/mnt/swap.1',
      swapfilesize => $swapfile_size,
    }
  },
}