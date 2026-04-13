-- V39: Add source_godown_id, destination_godown_id, transfer_reason to purchase_voucher_header (for Stock Journal)

ALTER TABLE purchase_voucher_header
    ADD COLUMN source_godown_id      BIGINT NULL AFTER process_description,
    ADD COLUMN destination_godown_id BIGINT NULL AFTER source_godown_id,
    ADD COLUMN transfer_reason       TEXT   NULL AFTER destination_godown_id;
