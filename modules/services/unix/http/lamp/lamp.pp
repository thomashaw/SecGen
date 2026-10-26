# Apache and MySQL are installed by the required modules (apache_stretch_compatible,
# mysql_stretch_compatible). Enable the PHP module for Apache and install the PHP
# MySQL extension so PHP scripts can talk to the database.
include ::apache::mod::php
ensure_packages(['php-mysql'])
