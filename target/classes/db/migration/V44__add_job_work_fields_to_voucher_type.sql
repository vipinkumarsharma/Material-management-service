ALTER TABLE voucher_type_master
    ADD COLUMN use_for_job_work      TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN use_for_job_work_in   TINYINT(1) NOT NULL DEFAULT 0;
