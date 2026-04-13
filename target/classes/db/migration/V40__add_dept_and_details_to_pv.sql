-- Add dept_id to PV header
ALTER TABLE purchase_voucher_header
  ADD COLUMN dept_id VARCHAR(20) DEFAULT NULL;

-- New destination-detail table
CREATE TABLE purchase_voucher_detail_to (
    pv_id           BIGINT        NOT NULL,
    item_id         VARCHAR(20)   NOT NULL,
    unit_id         VARCHAR(20),
    location_id     VARCHAR(20),
    qty             DECIMAL(15,4) DEFAULT 0,
    rate            DECIMAL(15,4) DEFAULT 0,
    gross_amount    DECIMAL(15,2) DEFAULT 0,
    discount_perc   DECIMAL(5,2)  DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    gst_perc        DECIMAL(5,2)  DEFAULT 0,
    gst_amount      DECIMAL(15,2) DEFAULT 0,
    cess_perc       DECIMAL(5,2)  DEFAULT 0,
    cess_amount     DECIMAL(15,2) DEFAULT 0,
    net_amount      DECIMAL(15,2) DEFAULT 0,
    ordered_qty     DECIMAL(15,4),
    line_narration  TEXT,
    PRIMARY KEY (pv_id, item_id),
    CONSTRAINT fk_pvdt_header FOREIGN KEY (pv_id) REFERENCES purchase_voucher_header(pv_id) ON DELETE CASCADE
);
