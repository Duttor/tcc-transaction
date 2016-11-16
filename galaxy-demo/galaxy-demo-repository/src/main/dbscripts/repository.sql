CREATE TABLE t_repository
(
  id numeric NOT NULL,
  category character varying(50),
  amount numeric NOT NULL,
  price numeric,
  CONSTRAINT t_repository_pkey PRIMARY KEY (id)
);

INSERT INTO t_repository(id,category,amount,price) values(1,'富盈十号',10000000,1);