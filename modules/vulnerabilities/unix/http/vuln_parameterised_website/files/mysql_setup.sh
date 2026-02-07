#!/bin/sh

USERNAME=${1}
PASSWORD=${2}

echo "GRANT ALL PRIVILEGES ON * . * TO '${USERNAME}'@'localhost';"| mysql --force
echo "CREATE DATABASE db;"| mysql --user=${USERNAME} --password=${PASSWORD} --force
mysql --force --user=${USERNAME} --password=${PASSWORD} db < ./db.sql
