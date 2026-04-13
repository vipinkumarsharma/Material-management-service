-- V27: Voucher Edit Log - immutable audit trail

CREATE TABLE voucher_edit_log (
    log_id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    entity_type     VARCHAR(30)  NOT NULL,
    entity_id       BIGINT       NOT NULL,
    voucher_number  VARCHAR(50),
    voucher_type_id VARCHAR(20),
    change_type     VARCHAR(30)  NOT NULL,
    field_name      VARCHAR(100),
    old_value       TEXT,
    new_value       TEXT,
    changed_by      VARCHAR(100),
    changed_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks         TEXT,

    INDEX idx_el_entity     (entity_type, entity_id),
    INDEX idx_el_changed_at (changed_at),
    INDEX idx_el_changed_by (changed_by)
);
