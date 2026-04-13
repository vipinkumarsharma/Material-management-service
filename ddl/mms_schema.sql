-- =====================================================
-- Material Management System - Complete DDL
-- Database: MySQL 8.x
-- =====================================================

-- Drop tables in reverse dters ependency order (if exists)
DROP TABLE IF EXISTS dept_transfer_detail;
DROP TABLE IF EXISTS dept_transfer_header;
DROP TABLE IF EXISTS issue_fifo_consumption;
DROP TABLE IF EXISTS issue_detail;
DROP TABLE IF EXISTS issue_header;
DROP TABLE IF EXISTS stock_transfer_detail;
DROP TABLE IF EXISTS stock_transfer_header;
DROP TABLE IF EXISTS material_stock_ledger;
DROP TABLE IF EXISTS branch_material_stock;
DROP TABLE IF EXISTS grn_detail;
DROP TABLE IF EXISTS grn_header;
DROP TABLE IF EXISTS po_detail;
DROP TABLE IF EXISTS po_header;
DROP TABLE IF EXISTS supplier_invoice;
DROP TABLE IF EXISTS approval_rule;
DROP TABLE IF EXISTS user_role_map;
DROP TABLE IF EXISTS role_master;
DROP TABLE IF EXISTS location_master;
DROP TABLE IF EXISTS branch_item_price;
DROP TABLE IF EXISTS item_master;
DROP TABLE IF EXISTS branch_department_map;
DROP TABLE IF EXISTS department_master;
DROP TABLE IF EXISTS supplier_master;
DROP TABLE IF EXISTS manufacturer_master;
DROP TABLE IF EXISTS unit_master;
DROP TABLE IF EXISTS sub_group_master;
DROP TABLE IF EXISTS group_master;
DROP TABLE IF EXISTS branch_master;

-- =====================================================
-- MASTER TABLES
-- =====================================================

-- Branch Master
CREATE TABLE branch_master (
    branch_id VARCHAR(20) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    gst_no VARCHAR(20),
    pincode VARCHAR(10),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Group Master
CREATE TABLE group_master (
    group_id VARCHAR(20) NOT NULL,
    group_desc VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sub Group Master
CREATE TABLE sub_group_master (
    group_id VARCHAR(20) NOT NULL,
    sub_group_id VARCHAR(20) NOT NULL,
    sub_group_desc VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, sub_group_id),
    CONSTRAINT fk_subgroup_group FOREIGN KEY (group_id) REFERENCES group_master(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Unit Master
CREATE TABLE unit_master (
    unit_id VARCHAR(20) NOT NULL,
    unit_desc VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Manufacturer Master
CREATE TABLE manufacturer_master (
    manuf_id VARCHAR(20) NOT NULL,
    manuf_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    address_3 VARCHAR(255),
    phone_1 VARCHAR(20),
    phone_2 VARCHAR(20),
    email VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (manuf_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Supplier Master
CREATE TABLE supplier_master (
    supp_id VARCHAR(20) NOT NULL,
    supp_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    address_3 VARCHAR(255),
    mob_no VARCHAR(20),
    phone_1 VARCHAR(20),
    email VARCHAR(100),
    agency VARCHAR(100),
    cont_pers VARCHAR(100),
    gstin VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (supp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Item Master
CREATE TABLE item_master (
    item_id VARCHAR(20) NOT NULL,
    item_desc VARCHAR(200) NOT NULL,
    group_id VARCHAR(20),
    sub_group_id VARCHAR(20),
    manuf_id VARCHAR(20),
    unit_id VARCHAR(20),
    gst_perc DECIMAL(5,2) DEFAULT 0,
    cost_price DECIMAL(15,4) DEFAULT 0,
    mrp DECIMAL(15,4) DEFAULT 0,
    hsn_code VARCHAR(20),
    cess_perc DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    CONSTRAINT fk_item_group FOREIGN KEY (group_id, sub_group_id) REFERENCES sub_group_master(group_id, sub_group_id),
    CONSTRAINT fk_item_manuf FOREIGN KEY (manuf_id) REFERENCES manufacturer_master(manuf_id),
    CONSTRAINT fk_item_unit FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Location Master
CREATE TABLE location_master (
    location_id VARCHAR(20) NOT NULL,
    branch_id VARCHAR(20) NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    parent_id VARCHAR(20),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (location_id),
    CONSTRAINT fk_location_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_location_parent FOREIGN KEY (parent_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role Master
CREATE TABLE role_master (
    role_id VARCHAR(20) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Role Map
CREATE TABLE user_role_map (
    user_id VARCHAR(50) NOT NULL,
    role_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_urm_role FOREIGN KEY (role_id) REFERENCES role_master(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Approval Rule
CREATE TABLE approval_rule (
    rule_id VARCHAR(20) NOT NULL,
    txn_type VARCHAR(20) NOT NULL COMMENT 'GRN / ISSUE / TRANSFER',
    condition_type VARCHAR(30) NOT NULL COMMENT 'PRICE_VARIANCE / QTY_VARIANCE',
    threshold_value DECIMAL(15,4) NOT NULL,
    required_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (rule_id),
    CONSTRAINT fk_approval_role FOREIGN KEY (required_role) REFERENCES role_master(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Department Master (auto-increment INT PK)
CREATE TABLE department_master (
    dept_id INT NOT NULL AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Branch-Department Mapping
CREATE TABLE branch_department_map (
    branch_id VARCHAR(20) NOT NULL,
    dept_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, dept_id),
    CONSTRAINT fk_bdm_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bdm_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Branch-Item Price (branch-level pricing overrides)
CREATE TABLE branch_item_price (
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    cost_price DECIMAL(15,4) NOT NULL,
    mrp DECIMAL(15,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, item_id),
    CONSTRAINT fk_bip_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bip_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- PURCHASE TABLES
-- =====================================================

-- Supplier Invoice
CREATE TABLE supplier_invoice (
    invoice_id BIGINT NOT NULL AUTO_INCREMENT,
    supp_id VARCHAR(20) NOT NULL,
    invoice_no VARCHAR(50) NOT NULL,
    invoice_date DATE NOT NULL,
    invoice_amount DECIMAL(15,4) NOT NULL DEFAULT 0,
    gst_amount DECIMAL(15,4) NOT NULL DEFAULT 0,
    net_amount DECIMAL(15,4) NOT NULL DEFAULT 0,
    invoice_s3_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (invoice_id),
    CONSTRAINT fk_invoice_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PO Header
CREATE TABLE po_header (
    po_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    supp_id VARCHAR(20) NOT NULL,
    po_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN / PARTIAL / CLOSED',
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (po_id),
    CONSTRAINT fk_po_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- PO Detail
CREATE TABLE po_detail (
    po_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    qty_ordered DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL,
    qty_received DECIMAL(15,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (po_id, item_id),
    CONSTRAINT fk_pod_po FOREIGN KEY (po_id) REFERENCES po_header(po_id),
    CONSTRAINT fk_pod_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INWARD (GRN) TABLES
-- =====================================================

-- GRN Header
CREATE TABLE grn_header (
    grn_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    dept_id INT NULL,
    supp_id VARCHAR(20) NOT NULL,
    po_id BIGINT,
    invoice_id BIGINT,
    challan_no VARCHAR(50),
    challan_date DATE,
    grn_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PENDING_APPROVAL / POSTED',
    remarks TEXT,
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (grn_id),
    CONSTRAINT fk_grn_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_grn_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id),
    CONSTRAINT fk_grn_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id),
    CONSTRAINT fk_grn_po FOREIGN KEY (po_id) REFERENCES po_header(po_id),
    CONSTRAINT fk_grn_invoice FOREIGN KEY (invoice_id) REFERENCES supplier_invoice(invoice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- GRN Detail
CREATE TABLE grn_detail (
    grn_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    unit_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_received DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL,
    gst_perc DECIMAL(5,2) DEFAULT 0,
    line_amount DECIMAL(15,4) NOT NULL,
    qty_remaining DECIMAL(15,4) NOT NULL COMMENT 'For FIFO tracking - remaining stock from this GRN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (grn_id, item_id),
    CONSTRAINT fk_grnd_grn FOREIGN KEY (grn_id) REFERENCES grn_header(grn_id),
    CONSTRAINT fk_grnd_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_grnd_unit FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id),
    CONSTRAINT fk_grnd_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- OUTWARD (ISSUE) TABLES
-- =====================================================

-- Issue Header
CREATE TABLE issue_header (
    issue_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    dept_id INT NULL,
    issue_date DATE NOT NULL,
    issued_to VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PENDING_APPROVAL / POSTED',
    issue_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR' COMMENT 'REGULAR / JOB_WORK',
    supp_id VARCHAR(20) NULL COMMENT 'Required for JOB_WORK',
    expected_return_date DATE NULL COMMENT 'For JOB_WORK tracking',
    remarks TEXT,
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (issue_id),
    CONSTRAINT fk_issue_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_issue_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id),
    CONSTRAINT fk_issue_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Issue Detail
CREATE TABLE issue_detail (
    issue_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_issued DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL COMMENT 'Derived from FIFO GRN rate',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (issue_id, item_id),
    CONSTRAINT fk_issued_issue FOREIGN KEY (issue_id) REFERENCES issue_header(issue_id),
    CONSTRAINT fk_issued_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_issued_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Issue FIFO Consumption (tracks which GRN batches were consumed)
CREATE TABLE issue_fifo_consumption (
    consumption_id BIGINT NOT NULL AUTO_INCREMENT,
    issue_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    grn_id BIGINT NOT NULL,
    qty_consumed DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (consumption_id),
    CONSTRAINT fk_fifo_issue FOREIGN KEY (issue_id, item_id) REFERENCES issue_detail(issue_id, item_id),
    CONSTRAINT fk_fifo_grn FOREIGN KEY (grn_id, item_id) REFERENCES grn_detail(grn_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INTER-BRANCH TRANSFER TABLES
-- =====================================================

-- Stock Transfer Header
CREATE TABLE stock_transfer_header (
    transfer_id BIGINT NOT NULL AUTO_INCREMENT,
    from_branch VARCHAR(20) NOT NULL,
    to_branch VARCHAR(20) NOT NULL,
    dept_id INT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED / IN_TRANSIT / RECEIVED',
    sender_issue_id BIGINT COMMENT 'Issue created at sender branch',
    receiver_grn_id BIGINT COMMENT 'GRN created at receiver branch',
    remarks TEXT,
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (transfer_id),
    CONSTRAINT fk_transfer_from_branch FOREIGN KEY (from_branch) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_transfer_to_branch FOREIGN KEY (to_branch) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_transfer_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id),
    CONSTRAINT fk_transfer_sender_issue FOREIGN KEY (sender_issue_id) REFERENCES issue_header(issue_id),
    CONSTRAINT fk_transfer_receiver_grn FOREIGN KEY (receiver_grn_id) REFERENCES grn_header(grn_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Transfer Detail
CREATE TABLE stock_transfer_detail (
    transfer_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    qty_sent DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0 COMMENT 'Transfer rate from sender FIFO',
    qty_received DECIMAL(15,4) DEFAULT 0 COMMENT 'Qty received at destination',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (transfer_id, item_id),
    CONSTRAINT fk_std_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfer_header(transfer_id),
    CONSTRAINT fk_std_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- DEPARTMENT TRANSFER TABLES
-- =====================================================

-- Department Transfer Header (intra-branch dept transfer)
CREATE TABLE dept_transfer_header (
    dept_transfer_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    from_dept_id INT NOT NULL,
    to_dept_id INT NOT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    remarks TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_transfer_id),
    CONSTRAINT fk_dth_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_dth_from_dept FOREIGN KEY (from_dept_id) REFERENCES department_master(dept_id),
    CONSTRAINT fk_dth_to_dept FOREIGN KEY (to_dept_id) REFERENCES department_master(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Department Transfer Detail
CREATE TABLE dept_transfer_detail (
    dept_transfer_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_transferred DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_transfer_id, item_id),
    CONSTRAINT fk_dtd_header FOREIGN KEY (dept_transfer_id) REFERENCES dept_transfer_header(dept_transfer_id),
    CONSTRAINT fk_dtd_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_dtd_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- STOCK TABLES
-- =====================================================

-- Material Stock Ledger (Single Source of Truth - NO UPDATE/DELETE)
CREATE TABLE material_stock_ledger (
    ledger_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    dept_id INT NULL,
    txn_date DATE NOT NULL,
    txn_type VARCHAR(30) NOT NULL COMMENT 'GRN / ISSUE / TRANSFER_IN / TRANSFER_OUT / DEPT_TRANSFER_IN / DEPT_TRANSFER_OUT / *_REVERSAL / *_CORRECTION',
    ref_id BIGINT NULL COMMENT 'Reference to GRN/Issue/Transfer ID (null for OPENING_BALANCE)',
    qty_in DECIMAL(15,4) NOT NULL DEFAULT 0,
    qty_out DECIMAL(15,4) NOT NULL DEFAULT 0,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0,
    balance_qty DECIMAL(15,4) NOT NULL,
    remarks VARCHAR(255) NULL,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ledger_id),
    CONSTRAINT fk_ledger_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_ledger_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_ledger_location FOREIGN KEY (location_id) REFERENCES location_master(location_id),
    CONSTRAINT fk_ledger_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Branch Material Stock (Summary View - Updated by GRN/Issue posting)
CREATE TABLE branch_material_stock (
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_on_hand DECIMAL(15,4) NOT NULL DEFAULT 0,
    avg_cost DECIMAL(15,4) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, item_id, location_id),
    CONSTRAINT fk_bms_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bms_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_bms_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

-- Master table indexes
CREATE INDEX idx_item_group ON item_master(group_id, sub_group_id);
CREATE INDEX idx_item_manuf ON item_master(manuf_id);
CREATE INDEX idx_location_branch ON location_master(branch_id);

-- Purchase indexes
CREATE INDEX idx_po_branch ON po_header(branch_id);
CREATE INDEX idx_po_supplier ON po_header(supp_id);
CREATE INDEX idx_po_date ON po_header(po_date);
CREATE INDEX idx_invoice_supplier ON supplier_invoice(supp_id);
CREATE INDEX idx_invoice_date ON supplier_invoice(invoice_date);

-- GRN indexes
CREATE INDEX idx_grn_branch ON grn_header(branch_id);
CREATE INDEX idx_grn_supplier ON grn_header(supp_id);
CREATE INDEX idx_grn_date ON grn_header(grn_date);
CREATE INDEX idx_grn_status ON grn_header(status);
CREATE INDEX idx_grn_po ON grn_header(po_id);
CREATE INDEX idx_grnd_item ON grn_detail(item_id);
CREATE INDEX idx_grnd_location ON grn_detail(location_id);

-- Issue indexes
CREATE INDEX idx_issue_branch ON issue_header(branch_id);
CREATE INDEX idx_issue_date ON issue_header(issue_date);
CREATE INDEX idx_issue_status ON issue_header(status);
CREATE INDEX idx_issued_item ON issue_detail(item_id);
CREATE INDEX idx_issued_location ON issue_detail(location_id);
CREATE INDEX idx_fifo_grn ON issue_fifo_consumption(grn_id, item_id);

-- Transfer indexes
CREATE INDEX idx_transfer_from ON stock_transfer_header(from_branch);
CREATE INDEX idx_transfer_to ON stock_transfer_header(to_branch);
CREATE INDEX idx_transfer_date ON stock_transfer_header(transfer_date);
CREATE INDEX idx_transfer_status ON stock_transfer_header(status);
CREATE INDEX idx_std_item ON stock_transfer_detail(item_id);

-- Department and pricing indexes
CREATE INDEX idx_bdm_dept ON branch_department_map(dept_id);
CREATE INDEX idx_bip_item ON branch_item_price(item_id);

-- Department transfer indexes
CREATE INDEX idx_dth_branch ON dept_transfer_header(branch_id);
CREATE INDEX idx_dth_from_dept ON dept_transfer_header(from_dept_id);
CREATE INDEX idx_dth_to_dept ON dept_transfer_header(to_dept_id);
CREATE INDEX idx_dth_date ON dept_transfer_header(transfer_date);
CREATE INDEX idx_dtd_item ON dept_transfer_detail(item_id);
CREATE INDEX idx_dtd_location ON dept_transfer_detail(location_id);

-- Stock ledger indexes
CREATE INDEX idx_ledger_branch_item ON material_stock_ledger(branch_id, item_id);
CREATE INDEX idx_ledger_location ON material_stock_ledger(location_id);
CREATE INDEX idx_ledger_dept ON material_stock_ledger(dept_id);
CREATE INDEX idx_ledger_txn_date ON material_stock_ledger(txn_date);
CREATE INDEX idx_ledger_txn_type ON material_stock_ledger(txn_type);
CREATE INDEX idx_ledger_ref ON material_stock_ledger(txn_type, ref_id);
CREATE INDEX idx_bms_item ON branch_material_stock(item_id);
CREATE INDEX idx_bms_location ON branch_material_stock(location_id);

-- New column indexes on altered tables
CREATE INDEX idx_grn_dept ON grn_header(dept_id);
CREATE INDEX idx_issue_dept ON issue_header(dept_id);
CREATE INDEX idx_issue_type ON issue_header(issue_type);
CREATE INDEX idx_issue_supp ON issue_header(supp_id);
CREATE INDEX idx_transfer_dept ON stock_transfer_header(dept_id);

-- =====================================================
-- SAMPLE DATA FOR TESTING
-- =====================================================

-- Insert sample roles
INSERT INTO role_master (role_id, role_name) VALUES
('ADMIN', 'Administrator'),
('MANAGER', 'Manager'),
('SUPERVISOR', 'Supervisor'),
('OPERATOR', 'Operator');

-- Insert sample approval rules
INSERT INTO approval_rule (rule_id, txn_type, condition_type, threshold_value, required_role) VALUES
('GRN_PRICE_5', 'GRN', 'PRICE_VARIANCE', 5.0000, 'SUPERVISOR'),
('GRN_PRICE_10', 'GRN', 'PRICE_VARIANCE', 10.0000, 'MANAGER'),
('GRN_PRICE_20', 'GRN', 'PRICE_VARIANCE', 20.0000, 'ADMIN');

-- Insert sample units
INSERT INTO unit_master (unit_id, unit_desc) VALUES
('PCS', 'Pieces'),
('KG', 'Kilograms'),
('LTR', 'Liters'),
('MTR', 'Meters'),
('BOX', 'Box'),
('PKT', 'Packet');

-- Insert sample branch
INSERT INTO branch_master (branch_id, branch_name, address_1, gst_no, pincode) VALUES
('BR001', 'Main Warehouse', '123 Industrial Area', '09AAACI1234A1Z5', '110001'),
('BR002', 'North Branch', '456 North Avenue', '09AAACI1234A2Z4', '110002');

-- Insert sample group and subgroup
INSERT INTO group_master (group_id, group_desc) VALUES
('GRP01', 'Raw Materials'),
('GRP02', 'Packaging Materials');

INSERT INTO sub_group_master (group_id, sub_group_id, sub_group_desc) VALUES
('GRP01', 'SG01', 'Dairy Products'),
('GRP01', 'SG02', 'Chemicals'),
('GRP02', 'SG01', 'Bottles'),
('GRP02', 'SG02', 'Cartons');

-- Insert sample manufacturer
INSERT INTO manufacturer_master (manuf_id, manuf_name, address_1) VALUES
('MFR001', 'ABC Manufacturing', 'Delhi'),
('MFR002', 'XYZ Industries', 'Mumbai');

-- Insert sample supplier (including TRANSFER for internal transfers)
INSERT INTO supplier_master (supp_id, supp_name, address_1, gstin) VALUES
('SUP001', 'Primary Supplier', '789 Supplier Street', '09BBBCI5678B1Z3'),
('SUP002', 'Secondary Supplier', '321 Vendor Lane', '09CCCDI9012C1Z2'),
('TRANSFER', 'Internal Transfer', 'N/A', 'N/A');

-- Insert sample items
INSERT INTO item_master (item_id, item_desc, group_id, sub_group_id, manuf_id, unit_id, gst_perc, cost_price, mrp, hsn_code) VALUES
('ITEM001', 'Fresh Milk 500ml', 'GRP01', 'SG01', 'MFR001', 'PCS', 5.00, 25.0000, 30.0000, '0401'),
('ITEM002', 'Butter 100g', 'GRP01', 'SG01', 'MFR001', 'PCS', 12.00, 45.0000, 55.0000, '0405'),
('ITEM003', 'PET Bottle 500ml', 'GRP02', 'SG01', 'MFR002', 'PCS', 18.00, 5.0000, 8.0000, '3923');

-- Insert sample locations
INSERT INTO location_master (location_id, branch_id, location_name, parent_id) VALUES
('LOC001', 'BR001', 'Cold Storage A', NULL),
('LOC002', 'BR001', 'Cold Storage B', NULL),
('LOC003', 'BR001', 'Dry Storage', NULL),
('LOC004', 'BR002', 'Cold Storage North', NULL),
('LOC005', 'BR002', 'Dry Storage North', NULL);

-- Insert sample user role mappings
INSERT INTO user_role_map (user_id, role_id) VALUES
('admin', 'ADMIN'),
('manager1', 'MANAGER'),
('supervisor1', 'SUPERVISOR'),
('operator1', 'OPERATOR');
-- =====================================================
-- V7: MIGRATION QUERIES (Run this section on existing DB)
-- Department Master, Branch-Level Pricing, Job Work
-- =====================================================

-- 1. New tables
CREATE TABLE IF NOT EXISTS department_master (
    dept_id INT NOT NULL AUTO_INCREMENT,
    dept_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS branch_department_map (
    branch_id VARCHAR(20) NOT NULL,
    dept_id INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, dept_id),
    CONSTRAINT fk_bdm_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bdm_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS branch_item_price (
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    cost_price DECIMAL(15,4) NOT NULL,
    mrp DECIMAL(15,4) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, item_id),
    CONSTRAINT fk_bip_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bip_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dept_transfer_header (
    dept_transfer_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    from_dept_id INT NOT NULL,
    to_dept_id INT NOT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'POSTED',
    remarks TEXT,
    created_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_transfer_id),
    CONSTRAINT fk_dth_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_dth_from_dept FOREIGN KEY (from_dept_id) REFERENCES department_master(dept_id),
    CONSTRAINT fk_dth_to_dept FOREIGN KEY (to_dept_id) REFERENCES department_master(dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dept_transfer_detail (
    dept_transfer_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_transferred DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (dept_transfer_id, item_id),
    CONSTRAINT fk_dtd_header FOREIGN KEY (dept_transfer_id) REFERENCES dept_transfer_header(dept_transfer_id),
    CONSTRAINT fk_dtd_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_dtd_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. ALTER existing tables: add dept_id + job work fields
ALTER TABLE grn_header ADD COLUMN dept_id INT NULL AFTER branch_id;
ALTER TABLE grn_header ADD CONSTRAINT fk_grn_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

ALTER TABLE issue_header ADD COLUMN dept_id INT NULL AFTER branch_id;
ALTER TABLE issue_header ADD COLUMN issue_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR' AFTER status;
ALTER TABLE issue_header ADD COLUMN supp_id VARCHAR(20) NULL AFTER issue_type;
ALTER TABLE issue_header ADD COLUMN expected_return_date DATE NULL AFTER supp_id;
ALTER TABLE issue_header ADD CONSTRAINT fk_issue_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);
ALTER TABLE issue_header ADD CONSTRAINT fk_issue_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id);

ALTER TABLE stock_transfer_header ADD COLUMN dept_id INT NULL AFTER to_branch;
ALTER TABLE stock_transfer_header ADD CONSTRAINT fk_transfer_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

ALTER TABLE material_stock_ledger ADD COLUMN dept_id INT NULL AFTER location_id;
ALTER TABLE material_stock_ledger ADD CONSTRAINT fk_ledger_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

-- 3. Indexes for new tables and columns
CREATE INDEX idx_bdm_dept ON branch_department_map(dept_id);
CREATE INDEX idx_bip_item ON branch_item_price(item_id);
CREATE INDEX idx_dth_branch ON dept_transfer_header(branch_id);
CREATE INDEX idx_dth_from_dept ON dept_transfer_header(from_dept_id);
CREATE INDEX idx_dth_to_dept ON dept_transfer_header(to_dept_id);
CREATE INDEX idx_dth_date ON dept_transfer_header(transfer_date);
CREATE INDEX idx_dtd_item ON dept_transfer_detail(item_id);
CREATE INDEX idx_dtd_location ON dept_transfer_detail(location_id);
CREATE INDEX idx_grn_dept ON grn_header(dept_id);
CREATE INDEX idx_issue_dept ON issue_header(dept_id);
CREATE INDEX idx_issue_type ON issue_header(issue_type);
CREATE INDEX idx_issue_supp ON issue_header(supp_id);
CREATE INDEX idx_transfer_dept ON stock_transfer_header(dept_id);
CREATE INDEX idx_ledger_dept ON material_stock_ledger(dept_id);

-- V8: Allow nullable ref_id for OPENING_BALANCE entries
ALTER TABLE material_stock_ledger MODIFY COLUMN ref_id BIGINT NULL;

-- V9: Stock correction support - widen txn_type, add remarks
ALTER TABLE material_stock_ledger MODIFY COLUMN txn_type VARCHAR(30) NOT NULL;
ALTER TABLE material_stock_ledger ADD COLUMN remarks VARCHAR(255) NULL AFTER balance_qty;