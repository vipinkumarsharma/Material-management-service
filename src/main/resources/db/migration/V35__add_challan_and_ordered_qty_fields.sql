-- V35: Add challan fields & linkedPoId (voucher number reference) to purchase_voucher_header; ordered_qty to purchase_voucher_detail

ALTER TABLE purchase_voucher_header
    ADD COLUMN challan_no   VARCHAR(100) NULL AFTER supplier_invoice_date,
    ADD COLUMN challan_date DATE         NULL AFTER challan_no,
    ADD COLUMN linked_po_id VARCHAR(100) NULL AFTER challan_date;

ALTER TABLE purchase_voucher_detail
    ADD COLUMN ordered_qty DECIMAL(15,4) NULL AFTER net_amount;