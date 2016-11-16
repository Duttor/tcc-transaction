-- Postgres
CREATE TABLE datasource_info
(
  id SERIAL,
  name character varying(64),
  driver_class character varying(64),
  jndi character varying(64),
  db_url character varying(256),
  username character varying(64),
  password character varying(64),
  max_active smallint default 10,
  initial_size smallint default 1,
  url character varying(256),
  status smallint default 1,
  active_flag smallint default 1,
  memo character varying(512),
  gmt_created timestamp,
  gmt_modified timestamp,
  CONSTRAINT ds_info_pkey PRIMARY KEY (id)
);

CREATE TABLE business_type
(
  id SERIAL,
  name character varying(64),
  ds_id character varying(128),
  active_flag smallint default 1,
  memo character varying(512),
  gmt_create timestamp,
  gmt_modified timestamp,
  CONSTRAINT ds_info_pkey PRIMARY KEY (id)
);

INSERT INTO datasource_info (NAME, driver_class, jndi, url, username, PASSWORD) VALUES ('udb1','', '', 'jdbc:mysql://127.0.0.1:3306/udb1?useUnicode=true&characterEncoding=utf8', 'root', 'root');

INSERT INTO transaction_info(tx_id, parent_id, business_id, business_type, tx_type, tx_status, context,  retried_count) VALUES (1, 1, 'business_id', 'test', 1, 1, 'context', 3);