create table Users(
    id varchar(128) primary key,
    name varchar(128) not null,
    tokenHash varchar(128) not null,
    admin boolean not null
);
