ALTER TABLE purchase_voucher_header
    ADD COLUMN consignee_entity_type VARCHAR(20) NULL,
    ADD COLUMN from_entity_type      VARCHAR(20) NULL;
