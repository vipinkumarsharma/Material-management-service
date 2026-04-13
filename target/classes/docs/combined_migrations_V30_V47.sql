-- =============================================================================
-- Combined Migrations: V30 → V47
-- Branch: AllTypVouchersImplementation
-- NOTE: V30, V31, V40 each have multiple files — all included below in order.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- V30 (1/3): V30__change_voucher_type_id_to_bigint.sql
-- -----------------------------------------------------------------------------
-- V30: Change voucher_type_id from VARCHAR(20) to BIGINT AUTO_INCREMENT

-- 1. Drop FK constraints that reference voucher_type_master(voucher_type_id)
ALTER TABLE voucher_series_master DROP FOREIGN KEY fk_vsm_voucher_type;
ALTER TABLE purchase_voucher_header DROP FOREIGN KEY fk_pvh_voucher_type;

-- 2. Change PRIMARY KEY column in voucher_type_master to BIGINT AUTO_INCREMENT
ALTER TABLE voucher_type_master MODIFY COLUMN voucher_type_id BIGINT NOT NULL AUTO_INCREMENT;

-- 3. Change FK columns in referencing tables to BIGINT
ALTER TABLE voucher_series_master MODIFY COLUMN voucher_type_id BIGINT NOT NULL;
ALTER TABLE purchase_voucher_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE grn_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE issue_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE po_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE dept_transfer_header MODIFY COLUMN voucher_type_id BIGINT;
ALTER TABLE stock_transfer_header MODIFY COLUMN voucher_type_id BIGINT;

-- 4. Re-add FK constraints
ALTER TABLE voucher_series_master
    ADD CONSTRAINT fk_vsm_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id);
ALTER TABLE purchase_voucher_header
    ADD CONSTRAINT fk_pvh_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id);


-- -----------------------------------------------------------------------------
-- V30 (2/3): V30__add_branch_code_to_branch_master.sql
-- -----------------------------------------------------------------------------
ALTER TABLE branch_master
    ADD COLUMN branch_code VARCHAR(20) NULL COMMENT 'Short alphanumeric code for the branch';

CREATE UNIQUE INDEX uq_branch_master_branch_code
    ON branch_master (branch_code);


-- -----------------------------------------------------------------------------
-- V30 (3/3): V30__add_dispatch_fields_to_purchase_voucher.sql
-- -----------------------------------------------------------------------------
ALTER TABLE purchase_voucher_header
    ADD COLUMN mode_of_payment     VARCHAR(100) NULL,
    ADD COLUMN other_references    VARCHAR(255) NULL,
    ADD COLUMN terms_of_delivery   VARCHAR(255) NULL,
    ADD COLUMN dispatch_through    VARCHAR(255) NULL,
    ADD COLUMN destination         VARCHAR(255) NULL,
    ADD COLUMN carrier_name_agent  VARCHAR(255) NULL,
    ADD COLUMN bill_of_lading_no   VARCHAR(100) NULL,
    ADD COLUMN motor_vehicle_no    VARCHAR(100) NULL;


-- -----------------------------------------------------------------------------
-- V31 (1/2): V31__rename_base_txn_type_to_voucher_category.sql
-- -----------------------------------------------------------------------------
ALTER TABLE voucher_type_master
    CHANGE COLUMN base_txn_type voucher_category VARCHAR(30) NOT NULL;


-- -----------------------------------------------------------------------------
-- V31 (2/2): V31__replace_po_with_pv_in_grn.sql
-- -----------------------------------------------------------------------------
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


-- -----------------------------------------------------------------------------
-- V32: V32__add_po_amount_break_point_to_purchase_voucher.sql
-- -----------------------------------------------------------------------------
ALTER TABLE purchase_voucher_header
    ADD COLUMN po_amount_break_point DECIMAL(15, 2) NULL;


-- -----------------------------------------------------------------------------
-- V33: V33__voucher_type_multi_branch.sql
-- -----------------------------------------------------------------------------
-- V33: Convert voucher_type_master.branch_id (single) to voucher_type_branch_map (multi-branch)

CREATE TABLE voucher_type_branch_map (
    voucher_type_id VARCHAR(20) NOT NULL,
    branch_id       VARCHAR(20) NOT NULL,
    PRIMARY KEY (voucher_type_id, branch_id),
    CONSTRAINT fk_vtbm_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id),
    CONSTRAINT fk_vtbm_branch       FOREIGN KEY (branch_id)       REFERENCES branch_master(branch_id)
);

-- Migrate existing single branch_id values
INSERT INTO voucher_type_branch_map (voucher_type_id, branch_id)
SELECT voucher_type_id, branch_id
FROM voucher_type_master
WHERE branch_id IS NOT NULL;

-- Drop FK and column from voucher_type_master
ALTER TABLE voucher_type_master
    DROP FOREIGN KEY fk_vt_branch,
    DROP COLUMN branch_id;


-- -----------------------------------------------------------------------------
-- V34: V34__purchase_voucher_po_consignee_fields.sql
-- -----------------------------------------------------------------------------
ALTER TABLE purchase_voucher_header
    ADD COLUMN voucher_category  VARCHAR(50)  NULL AFTER voucher_type_id,
    ADD COLUMN supplier_to_id    VARCHAR(20)  NULL AFTER supp_id,
    ADD COLUMN consignee_type    VARCHAR(20)  NULL AFTER supplier_to_id,
    ADD COLUMN is_third_party    TINYINT(1)   NOT NULL DEFAULT 0 AFTER consignee_type;


-- -----------------------------------------------------------------------------
-- V35: V35__add_challan_and_ordered_qty_fields.sql
-- -----------------------------------------------------------------------------
-- V35: Add challan fields & linkedPoId (voucher number reference) to purchase_voucher_header; ordered_qty to purchase_voucher_detail

ALTER TABLE purchase_voucher_header
    ADD COLUMN challan_no   VARCHAR(100) NULL AFTER supplier_invoice_date,
    ADD COLUMN challan_date DATE         NULL AFTER challan_no,
    ADD COLUMN linked_po_id VARCHAR(100) NULL AFTER challan_date;

ALTER TABLE purchase_voucher_detail
    ADD COLUMN ordered_qty DECIMAL(15,4) NULL AFTER net_amount;


-- -----------------------------------------------------------------------------
-- V36: V36__add_dispatch_tracking_delivery_to_pv.sql
-- -----------------------------------------------------------------------------
-- V36: Add dispatch_doc_no, tracking_no, delivery_address to purchase_voucher_header

ALTER TABLE purchase_voucher_header
    ADD COLUMN dispatch_doc_no  VARCHAR(100) NULL AFTER motor_vehicle_no,
    ADD COLUMN tracking_no      VARCHAR(100) NULL AFTER dispatch_doc_no,
    ADD COLUMN delivery_address TEXT         NULL AFTER tracking_no;


-- -----------------------------------------------------------------------------
-- V37: V37__add_due_date_process_desc_to_pv.sql
-- -----------------------------------------------------------------------------
-- V37: Add due_date and process_description to purchase_voucher_header (for Job Work In Order)

ALTER TABLE purchase_voucher_header
    ADD COLUMN due_date             DATE NULL AFTER delivery_address,
    ADD COLUMN process_description  TEXT NULL AFTER due_date;


-- -----------------------------------------------------------------------------
-- V38: V38__make_pv_supp_id_nullable.sql
-- -----------------------------------------------------------------------------
-- V38: Make supp_id nullable in purchase_voucher_header (supplier is optional for non-supplier voucher categories)

ALTER TABLE purchase_voucher_header
    MODIFY COLUMN supp_id VARCHAR(20) NULL;


-- -----------------------------------------------------------------------------
-- V39: V39__add_stock_journal_fields_to_pv.sql
-- -----------------------------------------------------------------------------
-- V39: Add source_godown_id, destination_godown_id, transfer_reason to purchase_voucher_header (for Stock Journal)

ALTER TABLE purchase_voucher_header
    ADD COLUMN source_godown_id      BIGINT NULL AFTER process_description,
    ADD COLUMN destination_godown_id BIGINT NULL AFTER source_godown_id,
    ADD COLUMN transfer_reason       TEXT   NULL AFTER destination_godown_id;


-- -----------------------------------------------------------------------------
-- V40 (1/2): V40__add_dept_and_details_to_pv.sql
-- -----------------------------------------------------------------------------
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


-- -----------------------------------------------------------------------------
-- V40 (2/2): V40__create_supplier_godown_map.sql
-- -----------------------------------------------------------------------------
-- V40: Supplier-Godown mapping table

CREATE TABLE supplier_godown_map (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    supp_id     VARCHAR(20)  NOT NULL,
    godown_id   BIGINT       NOT NULL,
    item_id     VARCHAR(20)  NULL,
    godown_name VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_supp_godown (supp_id, godown_id)
);


-- -----------------------------------------------------------------------------
-- V41: V41__add_reference_no_to_pv.sql
-- -----------------------------------------------------------------------------
-- V41: Add reference_no to purchase_voucher_header

ALTER TABLE purchase_voucher_header
    ADD COLUMN reference_no VARCHAR(100) NULL AFTER transfer_reason;


-- -----------------------------------------------------------------------------
-- V42: V42__add_purpose_of_issue_to_pv.sql
-- -----------------------------------------------------------------------------
-- V42: Add purpose_of_issue to purchase_voucher_header (for Material Out)

ALTER TABLE purchase_voucher_header
    ADD COLUMN purpose_of_issue TEXT NULL AFTER reference_no;


-- -----------------------------------------------------------------------------
-- V43: V43__remove_branch_id_from_voucher_series.sql
-- -----------------------------------------------------------------------------
ALTER TABLE voucher_series_master
    DROP FOREIGN KEY fk_vsm_branch,
    DROP INDEX uq_series_default,
    DROP COLUMN branch_id,
    ADD UNIQUE INDEX uq_series_default (voucher_type_id, is_default);


-- -----------------------------------------------------------------------------
-- V44: V44__add_job_work_fields_to_voucher_type.sql
-- -----------------------------------------------------------------------------
ALTER TABLE voucher_type_master
    ADD COLUMN use_for_job_work      TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN use_for_job_work_in   TINYINT(1) NOT NULL DEFAULT 0;


-- -----------------------------------------------------------------------------
-- V45: V45__create_godown_item_stock.sql
-- -----------------------------------------------------------------------------
CREATE TABLE godown_item_stock (
    godown_id   BIGINT        NOT NULL,
    item_id     VARCHAR(20)   NOT NULL,
    qty         DECIMAL(15,4) NOT NULL DEFAULT 0,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (godown_id, item_id)
);


-- -----------------------------------------------------------------------------
-- V46: V46__make_godown_id_nullable_in_supplier_godown_map.sql
-- -----------------------------------------------------------------------------
-- V46: Drop the separate godown_id column — the auto-generated id (PK) is the godown identifier
ALTER TABLE supplier_godown_map
    DROP INDEX uq_supp_godown,
    DROP COLUMN godown_id;


-- -----------------------------------------------------------------------------
-- V47: V47__add_department_and_supplier_to_voucher_type.sql
-- -----------------------------------------------------------------------------
ALTER TABLE voucher_type_master
  ADD COLUMN is_department TINYINT(1) NOT NULL DEFAULT 0 AFTER use_for_job_work_in,
  ADD COLUMN is_supplier TINYINT(1) NOT NULL DEFAULT 0 AFTER is_department;

-- Rollback (manual):
-- ALTER TABLE voucher_type_master DROP COLUMN is_department, DROP COLUMN is_supplier;
