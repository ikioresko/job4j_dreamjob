create table city (
    id SERIAL PRIMARY KEY,
    name TEXT
);
create TABLE post (
   id SERIAL PRIMARY KEY,
   name TEXT,
   description TEXT,
   created date
);
create TABLE candidates (
   id SERIAL PRIMARY KEY,
   name TEXT,
   city_id smallint REFERENCES city,
   created date
);
create TABLE users (
   id SERIAL PRIMARY KEY,
   name TEXT,
   email TEXT unique,
   password TEXT
);