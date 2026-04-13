ALTER TABLE dept_transfer_header
    ADD COLUMN is_received TINYINT(1) NOT NULL DEFAULT 0 AFTER transfer_out_id;
