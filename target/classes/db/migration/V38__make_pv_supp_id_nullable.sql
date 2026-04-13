-- V38: Make supp_id nullable in purchase_voucher_header (supplier is optional for non-supplier voucher categories)

ALTER TABLE purchase_voucher_header
    MODIFY COLUMN supp_id VARCHAR(20) NULL;
