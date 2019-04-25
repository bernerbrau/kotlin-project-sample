insert into accounts(login, display_name, pwd_hash)
values ('bernerbits', 'Derek Berner', crypt('correct horse battery staple', gen_salt('bf',12)));

insert into todos(account_id, description, due)
values (
         (select id from accounts where login = 'bernerbits'),
         'Mow the lawn',
         TIMESTAMP WITH TIME ZONE '2019-10-19 12:00:00-05:00'
       );

insert into todos(account_id, description, due)
values (
         (select id from accounts where login = 'bernerbits'),
         'Buy diapers',
         TIMESTAMP WITH TIME ZONE '2019-03-01 12:00:00-06:00'
       );
