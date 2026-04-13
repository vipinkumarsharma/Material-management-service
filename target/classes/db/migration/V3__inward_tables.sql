-- =====================================================
-- Material Management System - Database Schema
-- V3: Inward (GRN) Tables
-- =====================================================

-- GRN Header
CREATE TABLE grn_header (
    grn_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (grn_id),
    CONSTRAINT fk_grn_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (grn_id, item_id),
    CONSTRAINT fk_grnd_grn FOREIGN KEY (grn_id) REFERENCES grn_header(grn_id),
    CONSTRAINT fk_grnd_item FOREIGN KEY (item_id) REFERENCES item_master(item_id),
    CONSTRAINT fk_grnd_unit FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id),
    CONSTRAINT fk_grnd_location FOREIGN KEY (location_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_grn_branch ON grn_header(branch_id);
CREATE INDEX idx_grn_supplier ON grn_header(supp_id);
CREATE INDEX idx_grn_date ON grn_header(grn_date);
CREATE INDEX idx_grn_status ON grn_header(status);
CREATE INDEX idx_grn_po ON grn_header(po_id);
CREATE INDEX idx_grnd_item ON grn_detail(item_id);
CREATE INDEX idx_grnd_location ON grn_detail(location_id);
