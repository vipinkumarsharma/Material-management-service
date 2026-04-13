-- V24: Voucher Type Master, Voucher Series Master, Series Restart Schedule

CREATE TABLE voucher_type_master (
    voucher_type_id       VARCHAR(20) PRIMARY KEY,
    voucher_type_name     VARCHAR(100) NOT NULL,
    base_txn_type         VARCHAR(30)  NOT NULL,
    abbreviation          VARCHAR(10),
    is_active             TINYINT(1)   DEFAULT 1,

    numbering_method      VARCHAR(30)  DEFAULT 'AUTOMATIC',
    numbering_on_deletion VARCHAR(30)  DEFAULT 'RETAIN_ORIGINAL',
    show_unused_numbers   TINYINT(1)   DEFAULT 0,
    prevent_duplicates    TINYINT(1)   DEFAULT 1,

    allow_zero_valued     TINYINT(1)   DEFAULT 0,
    is_optional_default   TINYINT(1)   DEFAULT 0,
    use_effective_dates   TINYINT(1)   DEFAULT 0,
    effective_date_label  VARCHAR(50)  DEFAULT 'Effective Date',

    allow_narration       TINYINT(1)   DEFAULT 1,
    narration_mandatory   TINYINT(1)   DEFAULT 0,
    narration_per_line    TINYINT(1)   DEFAULT 0,

    print_after_save      TINYINT(1)   DEFAULT 0,

    require_approval      TINYINT(1)   DEFAULT 0,
    approval_amount_limit DECIMAL(15,2),

    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE voucher_series_master (
    series_id             VARCHAR(20)  PRIMARY KEY,
    series_name           VARCHAR(100) NOT NULL,
    voucher_type_id       VARCHAR(20)  NOT NULL,
    branch_id             VARCHAR(20)  NOT NULL,

    starting_number       INT          DEFAULT 1,
    current_number        INT          DEFAULT 1,

    number_width          INT          DEFAULT 6,
    prefill_with_zero     TINYINT(1)   DEFAULT 1,

    prefix_details        VARCHAR(100) DEFAULT '',
    suffix_details        VARCHAR(100) DEFAULT '',

    restart_periodicity   VARCHAR(20)  DEFAULT 'ANNUALLY',
    last_reset_date       DATE,
    next_restart_date     DATE,

    is_default            TINYINT(1)   DEFAULT 0,
    is_active             TINYINT(1)   DEFAULT 1,

    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_vsm_voucher_type FOREIGN KEY (voucher_type_id) REFERENCES voucher_type_master(voucher_type_id),
    CONSTRAINT fk_vsm_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    UNIQUE KEY uq_series_default (voucher_type_id, branch_id, is_default)
);

CREATE TABLE voucher_series_restart_schedule (
    restart_id            BIGINT       AUTO_INCREMENT PRIMARY KEY,
    series_id             VARCHAR(20)  NOT NULL,
    applicable_from_date  DATE         NOT NULL,
    starting_number       INT          DEFAULT 1,
    prefix_override       VARCHAR(100),
    suffix_override       VARCHAR(100),
    created_at            TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_vsrs_series FOREIGN KEY (series_id) REFERENCES voucher_series_master(series_id),
    INDEX idx_restart_series (series_id, applicable_from_date)
);
