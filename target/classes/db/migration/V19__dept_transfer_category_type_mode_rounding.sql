ALTER TABLE dept_transfer_header
    ADD COLUMN transfer_category VARCHAR(50)  NULL AFTER status,
    ADD COLUMN transfer_type     VARCHAR(20)  NULL AFTER transfer_category,
    ADD COLUMN transfer_mode     VARCHAR(50)  NULL AFTER transfer_type,
    ADD COLUMN rounding_type     VARCHAR(20)  NULL AFTER round_off_amount;
