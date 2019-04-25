#!/bin/sh

createdb todos
createuser -S -R -D app_user

psql -d todos -a -f $(dirname $(dirname $0))/sql/ddl.sql
psql -d todos -a -f $(dirname $(dirname $0))/sql/dml.sql

psql -c "alter user app_user with encrypted password 'correct horse battery staple';"
psql -c "grant all privileges on database todos to app_user;"

echo "listen_addresses = '*'" >> /etc/postgresql/9.1/main/postgresql.conf
echo "host all all 0.0.0.0/0 md5" >> /etc/postgresql/9.1/main/pg_hba.conf
/etc/init.d/postgresql restart
