-- Step 1: Add new columns as nullable
ALTER TABLE dept_transfer_header
    ADD COLUMN from_branch_id         VARCHAR(20) AFTER branch_id,
    ADD COLUMN to_branch_id           VARCHAR(20) AFTER from_branch_id,
    ADD COLUMN third_party_supplier_id VARCHAR(20) AFTER round_off_amount;

-- Step 2: Migrate existing branch_id into both new columns
UPDATE dept_transfer_header
SET from_branch_id = branch_id,
    to_branch_id   = branch_id;

-- Step 3: Apply NOT NULL constraints
ALTER TABLE dept_transfer_header
    MODIFY COLUMN from_branch_id VARCHAR(20) NOT NULL,
    MODIFY COLUMN to_branch_id   VARCHAR(20) NOT NULL;

-- Step 4: Add FK constraints for the new columns
ALTER TABLE dept_transfer_header
    ADD CONSTRAINT fk_dth_from_branch FOREIGN KEY (from_branch_id) REFERENCES branch_master(branch_id),
    ADD CONSTRAINT fk_dth_to_branch FOREIGN KEY (to_branch_id) REFERENCES branch_master(branch_id);

-- Step 5: Drop the old FK constraint and index, then drop the old branch_id column
ALTER TABLE dept_transfer_header DROP FOREIGN KEY fk_dth_branch;
DROP INDEX idx_dth_branch ON dept_transfer_header;
ALTER TABLE dept_transfer_header DROP COLUMN branch_id;
