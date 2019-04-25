CREATE EXTENSION pgcrypto;

create table accounts
(
	id bigserial not null
		constraint accounts_pk
			primary key,
	login varchar(16) not null,
	pwd_hash char(60) not null,
  display_name varchar
);

create unique index accounts_login_uindex
	on accounts (login);

grant all privileges on table accounts to app_user;

create table todos
(
	id bigserial not null
		constraint todos_pk
			primary key,
	account_id bigint not null
		constraint todos_accounts_id_fk
			references accounts,
	description text not null,
	priority int default 0 not null,
	due timestamp with time zone not null,
	done timestamp with time zone
);

grant all privileges on table todos to app_user;
