ALTER TABLE purchase_voucher_detail
    ADD COLUMN freight_amt         DECIMAL(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN gst_on_freight_amt  DECIMAL(15,4) NOT NULL DEFAULT 0;
