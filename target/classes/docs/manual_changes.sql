-- =============================================================================
-- Manual SQL Changes
-- These changes are NOT part of any single Flyway migration file.
-- Apply manually to the target database when needed.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- AllTypVouchersImplementation
-- -----------------------------------------------------------------------------

ALTER TABLE purchase_voucher_detail
    ADD COLUMN pre_close_qty DECIMAL(15,4) NULL DEFAULT NULL;


CREATE TABLE ob_location_header (
    ob_id       BIGINT NOT NULL AUTO_INCREMENT,
    branch_id   VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    cutoff_date DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (ob_id),
    UNIQUE KEY uq_ob_branch_location (branch_id, location_id),
    CONSTRAINT fk_ob_branch   FOREIGN KEY (branch_id)   REFERENCES branch_master(branch_id),
    CONSTRAINT fk_ob_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


ALTER TABLE purchase_voucher_header
    ADD COLUMN from_branch_id VARCHAR(20) NULL,
    ADD COLUMN consignee_id   VARCHAR(20) NULL,
    ADD CONSTRAINT fk_pvh_from_branch FOREIGN KEY (from_branch_id) REFERENCES branch_master(branch_id);


ALTER TABLE purchase_voucher_header
    ADD COLUMN consignee_entity_type VARCHAR(20) NULL,
    ADD COLUMN from_entity_type      VARCHAR(20) NULL;


ALTER TABLE purchase_voucher_detail
    ADD COLUMN freight_amt         DECIMAL(15,4) NOT NULL DEFAULT 0,
    ADD COLUMN gst_on_freight_amt  DECIMAL(15,4) NOT NULL DEFAULT 0;


ALTER TABLE voucher_type_master
    ADD COLUMN report_summary_title VARCHAR(200) DEFAULT NULL;