create table FavoriteSounds(
    userId varchar(128) not null,
    soundId varchar(128) not null,

    primary key (userId, soundId)
);