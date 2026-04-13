ALTER TABLE voucher_series_master
    DROP FOREIGN KEY fk_vsm_branch,
    DROP INDEX uq_series_default,
    DROP COLUMN branch_id,
    ADD UNIQUE INDEX uq_series_default (voucher_type_id, is_default);
