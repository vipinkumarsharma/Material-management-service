ALTER TABLE voucher_type_master
  ADD COLUMN is_department TINYINT(1) NOT NULL DEFAULT 0 AFTER use_for_job_work_in,
  ADD COLUMN is_supplier TINYINT(1) NOT NULL DEFAULT 0 AFTER is_department;

-- Rollback (manual):
-- ALTER TABLE voucher_type_master DROP COLUMN is_department, DROP COLUMN is_supplier;
