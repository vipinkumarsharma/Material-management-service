-- V46: Drop the separate godown_id column — the auto-generated id (PK) is the godown identifier
ALTER TABLE supplier_godown_map
    DROP INDEX uq_supp_godown,
    DROP COLUMN godown_id;