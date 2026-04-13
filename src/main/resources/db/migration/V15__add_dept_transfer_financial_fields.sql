-- dept_transfer_header: add summary financial fields
ALTER TABLE dept_transfer_header
    ADD COLUMN items_count  DECIMAL(15,4) NULL AFTER created_by,
    ADD COLUMN total_qty    DECIMAL(15,4) NULL AFTER items_count,
    ADD COLUMN gross_amount DECIMAL(15,4) NULL AFTER total_qty,
    ADD COLUMN net_amount   DECIMAL(15,4) NULL AFTER gross_amount;

-- dept_transfer_detail: add unit + financial fields
ALTER TABLE dept_transfer_detail
    ADD COLUMN unit_id         VARCHAR(20)   NULL          AFTER location_id,
    ADD COLUMN gross_amount    DECIMAL(15,4) NULL          AFTER rate,
    ADD COLUMN discount_perc   DECIMAL(5,2)  NOT NULL DEFAULT 0 AFTER gross_amount,
    ADD COLUMN discount_amount DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER discount_perc,
    ADD COLUMN gst_perc        DECIMAL(5,2)  NOT NULL DEFAULT 0 AFTER discount_amount,
    ADD COLUMN gst_amount      DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER gst_perc,
    ADD COLUMN cess_perc       DECIMAL(5,2)  NOT NULL DEFAULT 0 AFTER gst_amount,
    ADD COLUMN cess_amount     DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER cess_perc,
    ADD COLUMN net_amount      DECIMAL(15,4) NULL          AFTER cess_amount;

ALTER TABLE dept_transfer_detail
    ADD CONSTRAINT fk_dtd_unit FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id);
