ALTER TABLE dept_transfer_header
    ADD COLUMN round_off_amount DECIMAL(15,4) DEFAULT 0 AFTER net_amount;
