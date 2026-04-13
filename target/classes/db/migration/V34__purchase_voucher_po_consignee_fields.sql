ALTER TABLE purchase_voucher_header
    ADD COLUMN voucher_category  VARCHAR(50)  NULL AFTER voucher_type_id,
    ADD COLUMN supplier_to_id    VARCHAR(20)  NULL AFTER supp_id,
    ADD COLUMN consignee_type    VARCHAR(20)  NULL AFTER supplier_to_id,
    ADD COLUMN is_third_party    TINYINT(1)   NOT NULL DEFAULT 0 AFTER consignee_type;