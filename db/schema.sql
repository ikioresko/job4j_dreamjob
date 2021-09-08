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