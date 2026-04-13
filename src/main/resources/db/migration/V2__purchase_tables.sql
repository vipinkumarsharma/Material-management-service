-- =====================================================
-- Material Management System - Database Schema
-- V2: Purchase Tables
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (po_id, item_id),
    CONSTRAINT fk_pod_po FOREIGN KEY (po_id) REFERENCES po_header(po_id),
    CONSTRAINT fk_pod_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_po_branch ON po_header(branch_id);
CREATE INDEX idx_po_supplier ON po_header(supp_id);
CREATE INDEX idx_po_date ON po_header(po_date);
CREATE INDEX idx_invoice_supplier ON supplier_invoice(supp_id);
CREATE INDEX idx_invoice_date ON supplier_invoice(invoice_date);
