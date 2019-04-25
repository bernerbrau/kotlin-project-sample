#!/bin/sh

apt-get update
apt-get install -y postgresql postgresql-contrib
update-rc.d postgresql enable
service postgresql start

sudo -u postgres $(dirname $0)/psql.sh
