ALTER TABLE purchase_voucher_header
    ADD COLUMN from_branch_id VARCHAR(20) NULL,
    ADD COLUMN consignee_id   VARCHAR(20) NULL,
    ADD CONSTRAINT fk_pvh_from_branch FOREIGN KEY (from_branch_id) REFERENCES branch_master(branch_id);
