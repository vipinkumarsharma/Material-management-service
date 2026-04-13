ALTER TABLE branch_master
    ADD COLUMN branch_code VARCHAR(20) NULL COMMENT 'Short alphanumeric code for the branch';

CREATE UNIQUE INDEX uq_branch_master_branch_code
    ON branch_master (branch_code);