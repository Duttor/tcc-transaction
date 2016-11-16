CREATE TABLE t_order
(
  order_id numeric NOT NULL,
  product_id numeric NOT NULL,
  user_id numeric NOT NULL,
  status character varying(50),
  amount numeric NOT NULL,
  CONSTRAINT t_order_pkey PRIMARY KEY (order_id)
)
