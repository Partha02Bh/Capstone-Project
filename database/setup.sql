drop database if exists money_transfer_db;
create database money_transfer_db;
use money_transfer_db;

create table users(
	id bigint primary key,
    username varchar(50) unique not null,
    password varchar(100) not null,
    role varchar(20) not null,
    email varchar(100),                                                                                            
    otp_code varchar(6),
    otp_expiry datetime
);

create table accounts(
	id bigint auto_increment primary key,
    user_id bigint not null,
    account_number varchar(20) unique not null,
    balance decimal(15,2) default 0.00,
    status varchar(20) default 'ACTIVE',
    version int default 0,
    last_updated datetime default current_timestamp,
    foreign key (user_id) references users(id)
);

create table transactions (
	id bigint auto_increment primary key,
    source_account_id bigint,
    target_account_id bigint,
    amount decimal(15,2) not null,
    status varchar(20) not null,
    idempotency_key varchar(100) unique,
    created_at datetime default current_timestamp,
    foreign key (source_account_id) references accounts(id),
    foreign key (target_account_id) references accounts(id)
);
insert into users(id, username, password, role, email) values (1,'admin','admin123','OWNER', 'admin@bank.com');
insert into users(id, username, password, role, email) values (2,'john','john123','USER', 'john@gmail.com');
insert into users(id, username, password, role, email) values (3,'alice','alice123','USER', 'alice@gmail.com');



insert into accounts(id, user_id, account_number, balance) values (1, 2,'ACC1001',5000.00);
insert into accounts(id, user_id, account_number, balance) values (2, 3,'ACC1002',1000.00);

