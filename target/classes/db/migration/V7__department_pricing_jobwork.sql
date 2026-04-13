-- =====================================================
-- V7: Department Master, Branch-Level Pricing, Job Work
-- =====================================================

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
-- ALTER existing tables: add dept_id
-- =====================================================

-- GRN Header: optional department tagging
ALTER TABLE grn_header ADD COLUMN dept_id INT NULL AFTER branch_id;
ALTER TABLE grn_header ADD CONSTRAINT fk_grn_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

-- Issue Header: department tagging + job work fields
ALTER TABLE issue_header ADD COLUMN dept_id INT NULL AFTER branch_id;
ALTER TABLE issue_header ADD COLUMN issue_type VARCHAR(20) NOT NULL DEFAULT 'REGULAR' AFTER status;
ALTER TABLE issue_header ADD COLUMN supp_id VARCHAR(20) NULL AFTER issue_type;
ALTER TABLE issue_header ADD COLUMN expected_return_date DATE NULL AFTER supp_id;
ALTER TABLE issue_header ADD CONSTRAINT fk_issue_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);
ALTER TABLE issue_header ADD CONSTRAINT fk_issue_supplier FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id);

-- Stock Transfer Header: optional department tagging
ALTER TABLE stock_transfer_header ADD COLUMN dept_id INT NULL AFTER to_branch;
ALTER TABLE stock_transfer_header ADD CONSTRAINT fk_transfer_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

-- Material Stock Ledger: optional department tagging
ALTER TABLE material_stock_ledger ADD COLUMN dept_id INT NULL AFTER location_id;
ALTER TABLE material_stock_ledger ADD CONSTRAINT fk_ledger_dept FOREIGN KEY (dept_id) REFERENCES department_master(dept_id);

-- =====================================================
-- INDEXES for new tables and columns
-- =====================================================

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
