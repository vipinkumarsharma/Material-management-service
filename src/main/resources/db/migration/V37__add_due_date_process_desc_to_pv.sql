-- V37: Add due_date and process_description to purchase_voucher_header (for Job Work In Order)

ALTER TABLE purchase_voucher_header
    ADD COLUMN due_date             DATE NULL AFTER delivery_address,
    ADD COLUMN process_description  TEXT NULL AFTER due_date;
