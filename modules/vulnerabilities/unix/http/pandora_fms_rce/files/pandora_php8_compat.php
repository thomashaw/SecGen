<?php
// Compatibility shims so the Pandora FMS 7.0NG.742 console (written for PHP 7)
// runs on PHP 8. Only applied to the Pandora console directory.
if (!function_exists('get_magic_quotes_gpc')) {
    function get_magic_quotes_gpc() { return false; }
}
if (!function_exists('create_function')) {
    function create_function($args, $code) { return eval('return function(' . $args . ') {' . $code . '};'); }
}
if (!function_exists('each')) {
    function each(&$array) {
        $key = key($array);
        if ($key === null) { return false; }
        $value = current($array);
        next($array);
        return array(1 => $value, 'value' => $value, 0 => $key, 'key' => $key);
    }
}
