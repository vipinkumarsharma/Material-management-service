-- =====================================================
-- V8: Allow nullable ref_id for OPENING_BALANCE entries
-- =====================================================

ALTER TABLE material_stock_ledger MODIFY COLUMN ref_id BIGINT NULL;
