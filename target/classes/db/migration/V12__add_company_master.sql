CREATE TABLE company_master (
    company_id VARCHAR(20) NOT NULL,
    company_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (company_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

ALTER TABLE branch_master
    ADD COLUMN company_id VARCHAR(20) NULL,
    ADD CONSTRAINT fk_branch_company FOREIGN KEY (company_id) REFERENCES company_master(company_id);

ALTER TABLE item_master
    ADD COLUMN company_id VARCHAR(20) NULL,
    ADD CONSTRAINT fk_item_company FOREIGN KEY (company_id) REFERENCES company_master(company_id);

CREATE INDEX idx_branch_company ON branch_master(company_id);
CREATE INDEX idx_item_company ON item_master(company_id);
