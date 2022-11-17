-- Create a keyspace
CREATE KEYSPACE IF NOT EXISTS sample_keyspace WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : '1' };

-- Create a table
CREATE TABLE IF NOT EXISTS sample_keyspace.shopping_cart (
userid text PRIMARY KEY,
item_count int,
last_update_timestamp timestamp
);

-- Create a table
CREATE TABLE IF NOT EXISTS sample_keyspace.sample_table (
id int,
town text,
name text,
PRIMARY KEY(id, town)
);

CREATE MATERIALIZED VIEW IF NOT EXISTS sample_keyspace.mv_sample_table
AS SELECT town, name, id
FROM sample_keyspace.sample_table
WHERE town IS NOT NULL
  AND name IS NOT NULL
  AND id IS NOT NULL
  AND town IN ('Earth', 'Moon')
PRIMARY KEY(town, id);


-- Insert some data
INSERT INTO sample_keyspace.shopping_cart
(userid, item_count, last_update_timestamp)
VALUES ('9876', 2, toTimeStamp(now()));
INSERT INTO sample_keyspace.shopping_cart
(userid, item_count, last_update_timestamp)
VALUES ('1234', 5, toTimeStamp(now()));

INSERT INTO sample_keyspace.sample_table
(id, name, town)
VALUES (1, 'Jim', 'Earth');
INSERT INTO sample_keyspace.sample_table
(id, name, town)
VALUES (2, 'Carl', 'Moon');
INSERT INTO sample_keyspace.sample_table
(id, name, town)
VALUES (3, 'Carl', 'Mars');
INSERT INTO sample_keyspace.sample_table
(id, name, town)
VALUES (4, 'David', 'Moon');
INSERT INTO sample_keyspace.sample_table
(id, name, town)
VALUES (5, 'John', 'Canada');
