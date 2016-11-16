CREATE TABLE transaction_info
(
  parent_id BIGINT,
  tx_id BIGINT,
  module_id CHARACTER VARYING(64),
  business_id CHARACTER VARYING(64),
  business_type CHARACTER VARYING(64),
  tx_type SMALLINT,
  tx_status SMALLINT,
  context TEXT,
  retried_count CHARACTER VARYING(128),
  next_retry_time TIMESTAMP,
  gmt_created TIMESTAMP,
  gmt_modified TIMESTAMP,
  CONSTRAINT tran_info_pkey PRIMARY KEY (parent_id, tx_id)
);
CREATE INDEX idx_tran_info_mid ON transaction_info(module_id);
CREATE INDEX idx_tran_info_ts ON transaction_info(tx_status);
CREATE INDEX idx_tran_info_nrt ON transaction_info(next_retry_time);
CREATE INDEX idx_tran_info_gm ON transaction_info(gmt_modified);

COMMIT;