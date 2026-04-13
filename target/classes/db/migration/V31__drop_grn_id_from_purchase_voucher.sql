ALTER TABLE purchase_voucher_header
    DROP FOREIGN KEY fk_pvh_grn,
    DROP INDEX idx_pv_grn,
    DROP COLUMN grn_id;
