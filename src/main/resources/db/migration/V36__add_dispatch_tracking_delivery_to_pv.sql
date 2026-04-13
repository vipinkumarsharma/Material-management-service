-- V36: Add dispatch_doc_no, tracking_no, delivery_address to purchase_voucher_header

ALTER TABLE purchase_voucher_header
    ADD COLUMN dispatch_doc_no  VARCHAR(100) NULL AFTER motor_vehicle_no,
    ADD COLUMN tracking_no      VARCHAR(100) NULL AFTER dispatch_doc_no,
    ADD COLUMN delivery_address TEXT         NULL AFTER tracking_no;