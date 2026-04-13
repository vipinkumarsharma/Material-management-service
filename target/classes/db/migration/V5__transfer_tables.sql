-- =====================================================
-- Material Management System - Database Schema
-- V5: Inter-Branch Transfer Tables
-- =====================================================

-- Stock Transfer Header
CREATE TABLE stock_transfer_header (
    transfer_id BIGINT NOT NULL AUTO_INCREMENT,
    from_branch VARCHAR(20) NOT NULL,
    to_branch VARCHAR(20) NOT NULL,
    transfer_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED / IN_TRANSIT / RECEIVED',
    sender_issue_id BIGINT COMMENT 'Issue created at sender branch',
    receiver_grn_id BIGINT COMMENT 'GRN created at receiver branch',
    remarks TEXT,
    created_by VARCHAR(50),
    approved_by VARCHAR(50),
    approved_at TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (transfer_id),
    CONSTRAINT fk_transfer_from_branch FOREIGN KEY (from_branch) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_transfer_to_branch FOREIGN KEY (to_branch) REFERENCES branch_master(branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Stock Transfer Detail
CREATE TABLE stock_transfer_detail (
    transfer_id BIGINT NOT NULL,
    item_id VARCHAR(20) NOT NULL,
    qty_sent DECIMAL(15,4) NOT NULL,
    rate DECIMAL(15,4) NOT NULL DEFAULT 0 COMMENT 'Transfer rate from sender FIFO',
    qty_received DECIMAL(15,4) DEFAULT 0 COMMENT 'Qty received at destination',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (transfer_id, item_id),
    CONSTRAINT fk_std_transfer FOREIGN KEY (transfer_id) REFERENCES stock_transfer_header(transfer_id),
    CONSTRAINT fk_std_item FOREIGN KEY (item_id) REFERENCES item_master(item_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes
CREATE INDEX idx_transfer_from ON stock_transfer_header(from_branch);
CREATE INDEX idx_transfer_to ON stock_transfer_header(to_branch);
CREATE INDEX idx_transfer_date ON stock_transfer_header(transfer_date);
CREATE INDEX idx_transfer_status ON stock_transfer_header(status);
CREATE INDEX idx_std_item ON stock_transfer_detail(item_id);

-- Add foreign keys to link sender issue and receiver GRN (deferred as tables created earlier)
ALTER TABLE stock_transfer_header
ADD CONSTRAINT fk_transfer_sender_issue FOREIGN KEY (sender_issue_id) REFERENCES issue_header(issue_id);
