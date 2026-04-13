-- V41: Add reference_no to purchase_voucher_header

ALTER TABLE purchase_voucher_header
    ADD COLUMN reference_no VARCHAR(100) NULL AFTER transfer_reason;