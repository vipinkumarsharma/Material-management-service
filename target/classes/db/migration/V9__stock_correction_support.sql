-- =====================================================
-- V9: Stock correction support - widen txn_type, add remarks
-- =====================================================

ALTER TABLE material_stock_ledger MODIFY COLUMN txn_type VARCHAR(30) NOT NULL;
ALTER TABLE material_stock_ledger ADD COLUMN remarks VARCHAR(255) NULL AFTER balance_qty;
