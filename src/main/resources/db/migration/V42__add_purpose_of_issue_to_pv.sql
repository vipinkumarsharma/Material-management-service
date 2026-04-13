-- V42: Add purpose_of_issue to purchase_voucher_header (for Material Out)

ALTER TABLE purchase_voucher_header
    ADD COLUMN purpose_of_issue TEXT NULL AFTER reference_no;