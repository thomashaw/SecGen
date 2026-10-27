# Apache and MySQL are installed by the required modules (services/unix/http/apache
# and database/mysql on Debian, the *_kali_compatible pair on Kali). Enable the PHP
# module for Apache and install the PHP MySQL extension so PHP scripts can talk to
# the database.
class lamp::config {
  include ::apache::mod::php
  ensure_packages(['php-mysql'])
}
