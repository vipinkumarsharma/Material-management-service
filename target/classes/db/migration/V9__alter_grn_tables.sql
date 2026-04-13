-- =====================================================
-- V9: GRN Header & Detail - new summary and breakdown columns
-- =====================================================

-- ---- grn_header ----

-- Add summary amount / count columns
ALTER TABLE grn_header
    ADD COLUMN items_count  DECIMAL(15,4) NULL AFTER remarks,
    ADD COLUMN total_qty    DECIMAL(15,4) NULL AFTER items_count,
    ADD COLUMN gross_amount DECIMAL(15,4) NULL AFTER total_qty,
    ADD COLUMN net_amount   DECIMAL(15,4) NULL AFTER gross_amount;

-- ---- grn_detail ----

-- Rename line_amount -> gross_amount
ALTER TABLE grn_detail
    CHANGE COLUMN line_amount gross_amount DECIMAL(15,4) NOT NULL;

-- Add GST breakdown, discount, cess, net columns
ALTER TABLE grn_detail
    ADD COLUMN gst_amount      DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER gst_perc,
    ADD COLUMN discount_perc   DECIMAL(5,2)  NOT NULL DEFAULT 0 AFTER gst_amount,
    ADD COLUMN discount_amount DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER discount_perc,
    ADD COLUMN cess_perc       DECIMAL(5,2)  NOT NULL DEFAULT 0 AFTER discount_amount,
    ADD COLUMN cess_amount     DECIMAL(15,4) NOT NULL DEFAULT 0 AFTER cess_perc,
    ADD COLUMN net_amount      DECIMAL(15,4) NULL     AFTER cess_amount;