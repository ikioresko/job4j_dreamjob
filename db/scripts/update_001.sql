create table if not exists city(
    id SERIAL PRIMARY KEY,
    name TEXT
);

insert into city(name) values('Moscow');

create TABLE if not exists post(
   id SERIAL PRIMARY KEY,
   name TEXT,
   description TEXT,
   created TIMESTAMP with time zone
);

create TABLE if not exists candidates(
   id SERIAL PRIMARY KEY,
   name TEXT,
   city_id smallint,
   created TIMESTAMP with time zone,
   foreign key (city_id) REFERENCES city(id)
);