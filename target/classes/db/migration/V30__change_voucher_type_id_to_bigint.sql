-- V30: Change voucher_type_id from VARCHAR(20) to BIGINT AUTO_INCREMENT

-- 1. Drop FK constraints that reference voucher_type_master(voucher_type_id)
ALTER TABLE voucher_series_master DROP FOREIGN KEY fk_vsm_voucher_type;
ALTER TABLE purchase_voucher_header DROP FOREIGN KEY fk_pvh_voucher_type;

-- 2. Change PRIMARY KEY column in voucher_type_master to BIGINT AUTO_INCREMENT
ALTER TABLE voucher_type_master MODIFY COLUMN voucher_type_id BIGINT NOT NULL AUTO_INCREMENT;

-- 3. Change FK columns in referencing tables to BIGINT
ALTER TABLE voucher_series_master MODIFY COLUMN voucher_type_id BIGINT NOT NULL;
ALTER TABLE purchase_voucher_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE grn_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE issue_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE po_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE dept_transfer_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE stock_transfer_header MODIFY COLUMN voucher_type_id BIGINT;

-- 4. Re-add FK constraints
ALTER TABLE voucher_series_master
    ADD CONSTRAINT fk_vsm_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id);
ALTER TABLE purchase_voucher_header
    ADD CONSTRAINT fk_pvh_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id);