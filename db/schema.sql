create TABLE post (
   id SERIAL PRIMARY KEY,
   name TEXT,
   description TEXT,
   created TIMESTAMP
);
create TABLE candidates (
   id SERIAL PRIMARY KEY,
   name TEXT
);
create TABLE users (
   id SERIAL PRIMARY KEY,
   name TEXT,
   email TEXT unique,
   password TEXT
);