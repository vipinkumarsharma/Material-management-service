ALTER TABLE voucher_type_master
    CHANGE COLUMN base_txn_type voucher_category VARCHAR(30) NOT NULL;