CREATE TABLE godown_item_stock (
    godown_id   BIGINT        NOT NULL,
    item_id     VARCHAR(20)   NOT NULL,
    qty         DECIMAL(15,4) NOT NULL DEFAULT 0,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (godown_id, item_id)
);
