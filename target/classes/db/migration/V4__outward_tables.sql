-- =====================================================
-- Material Management System - Database Schema
-- V4: Outward (Issue) Tables
-- =====================================================

-- Issue Header
CREATE TABLE issue_header (
    issue_id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id VARCHAR(20) NOT NULL,
    issue_date DATE NOT NULL,
    issued_to VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PENDING_APPROVAL / POSTED',
    remarks TEXT,
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (issue_id),
    CONSTRAINT fk_issue_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Issue Detail
CREATE TABLE issue_detail (
    issue_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    location_id VARCHAR(20) NOT NULL,
    qty_issued DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL COMMENT 'Derived from FIFO GRN rate',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (consumption_id),
    CONSTRAINT fk_fifo_issue FOREIGN KEY (issue_id, item_id) REFERENCES issue_detail(issue_id, item_id),
    CONSTRAINT fk_fifo_grn FOREIGN KEY (grn_id, item_id) REFERENCES grn_detail(grn_id, item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_issue_branch ON issue_header(branch_id);
CREATE INDEX idx_issue_date ON issue_header(issue_date);
CREATE INDEX idx_issue_status ON issue_header(status);
CREATE INDEX idx_issued_item ON issue_detail(item_id);
CREATE INDEX idx_issued_location ON issue_detail(location_id);
CREATE INDEX idx_fifo_grn ON issue_fifo_consumption(grn_id, item_id);
