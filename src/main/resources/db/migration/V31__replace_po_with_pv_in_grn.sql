-- grn_header: drop PO FK + column, add pv_id FK
ALTER TABLE grn_header
    DROP FOREIGN KEY fk_grn_po,
    DROP COLUMN po_id,
    ADD COLUMN pv_id BIGINT NULL,
    ADD CONSTRAINT fk_grn_pv FOREIGN KEY (pv_id) REFERENCES purchase_voucher_header(pv_id);

-- purchase_voucher_header: drop PO FK + column
ALTER TABLE purchase_voucher_header
    DROP FOREIGN KEY fk_pvh_po,
    DROP COLUMN po_id;
