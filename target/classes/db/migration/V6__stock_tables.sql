-- =====================================================
-- Material Management System - Database Schema
-- V6: Stock Ledger and Summary Tables
-- =====================================================

-- Material Stock Ledger (Single Source of Truth - NO UPDATE/DELETE)
CREATE TABLE material_stock_ledger (
    ledger_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    txn_date DATE NOT NULL,
    txn_type VARCHAR(20) NOT NULL COMMENT 'GRN / ISSUE / TRANSFER_IN / TRANSFER_OUT',
    ref_id BIGINT NOT NULL COMMENT 'Reference to GRN/Issue/Transfer ID',
    qty_in DECIMAL(15,4) NOT NULL DEFAULT 0,
    qty_out DECIMAL(15,4) NOT NULL DEFAULT 0,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0,
    balance_qty DECIMAL(15,4) NOT NULL,
    created_on TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (ledger_id),
    CONSTRAINT fk_ledger_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_ledger_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_ledger_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Branch Material Stock (Summary View - Updated by GRN/Issue posting)
CREATE TABLE branch_material_stock (
    branch_id VARCHAR(20) NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_on_hand DECIMAL(15,4) NOT NULL DEFAULT 0,
    avg_cost DECIMAL(15,4) NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id, item_id, location_id),
    CONSTRAINT fk_bms_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_bms_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_bms_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for performance
CREATE INDEX idx_ledger_branch_item ON material_stock_ledger(branch_id, item_id);
CREATE INDEX idx_ledger_location ON material_stock_ledger(location_id);
CREATE INDEX idx_ledger_txn_date ON material_stock_ledger(txn_date);
CREATE INDEX idx_ledger_txn_type ON material_stock_ledger(txn_type);
CREATE INDEX idx_ledger_ref ON material_stock_ledger(txn_type, ref_id);
CREATE INDEX idx_bms_item ON branch_material_stock(item_id);
CREATE INDEX idx_bms_location ON branch_material_stock(location_id);

-- Add FK for receiver GRN in transfer (after grn_header exists)
ALTER TABLE stock_transfer_header
ADD CONSTRAINT fk_transfer_receiver_grn FOREIGN KEY (receiver_grn_id) REFERENCES grn_header(grn_id);
