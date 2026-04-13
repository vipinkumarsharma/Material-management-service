-- V25: Add voucher_number, voucher_type_id, effective_date to all transaction headers

ALTER TABLE grn_header
    ADD COLUMN voucher_number  VARCHAR(50) AFTER grn_id,
    ADD COLUMN voucher_type_id VARCHAR(20) AFTER voucher_number,
    ADD COLUMN effective_date  DATE        AFTER grn_date;

ALTER TABLE issue_header
    ADD COLUMN voucher_number  VARCHAR(50) AFTER issue_id,
    ADD COLUMN voucher_type_id VARCHAR(20) AFTER voucher_number,
    ADD COLUMN effective_date  DATE        AFTER issue_date;

ALTER TABLE po_header
    ADD COLUMN voucher_number  VARCHAR(50) AFTER po_id,
    ADD COLUMN voucher_type_id VARCHAR(20) AFTER voucher_number,
    ADD COLUMN effective_date  DATE        AFTER po_date;

ALTER TABLE dept_transfer_header
    ADD COLUMN voucher_number  VARCHAR(50) AFTER dept_transfer_id,
    ADD COLUMN voucher_type_id VARCHAR(20) AFTER voucher_number,
    ADD COLUMN effective_date  DATE        AFTER transfer_date;

ALTER TABLE stock_transfer_header
    ADD COLUMN voucher_number  VARCHAR(50) AFTER transfer_id,
    ADD COLUMN voucher_type_id VARCHAR(20) AFTER voucher_number,
    ADD COLUMN effective_date  DATE        AFTER transfer_date;
