#!/bin/sh

USERNAME=${1}
PASSWORD=${2}

echo "GRANT ALL PRIVILEGES ON * . * TO '${USERNAME}'@'localhost';"| mysql --force
echo "CREATE DATABASE site;"| mysql --user=${USERNAME} --password=${PASSWORD} --force
mysql --force --user=${USERNAME} --password=${PASSWORD} site < ./db.sql
