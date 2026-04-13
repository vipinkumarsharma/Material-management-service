-- V40: Supplier-Godown mapping table

CREATE TABLE supplier_godown_map (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    supp_id     VARCHAR(20)  NOT NULL,
    godown_id   BIGINT       NOT NULL,
    item_id     VARCHAR(20)  NULL,
    godown_name VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uq_supp_godown (supp_id, godown_id)
);
