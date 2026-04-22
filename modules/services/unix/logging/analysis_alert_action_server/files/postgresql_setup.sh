#!/bin/sh

USERNAME=${1}
PASSWORD=${2}
sudo -u postgres createuser ${USERNAME}
sudo -u postgres psql -c "DROP DATABASE IF EXISTS alert_events;"
sudo -u postgres psql -c "CREATE DATABASE alert_events;"
sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE alert_events TO ${USERNAME};"
sudo -u postgres psql alert_events < ./lib/alert_events.sql