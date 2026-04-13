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