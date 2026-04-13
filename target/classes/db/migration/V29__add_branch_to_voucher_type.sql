ALTER TABLE voucher_type_master
  ADD COLUMN branch_id VARCHAR(20) DEFAULT NULL AFTER voucher_type_id,
  ADD CONSTRAINT fk_vt_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id);
