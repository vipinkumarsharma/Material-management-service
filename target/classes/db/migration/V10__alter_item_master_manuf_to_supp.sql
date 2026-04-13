-- Rename manuf_id to supp_id in item_master and update FK to reference supplier_master

ALTER TABLE item_master DROP FOREIGN KEY fk_item_manuf;
DROP INDEX idx_item_manuf ON item_master;
ALTER TABLE item_master CHANGE COLUMN manuf_id supp_id VARCHAR(20);
ALTER TABLE item_master ADD CONSTRAINT fk_item_supp FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id);
CREATE INDEX idx_item_supp ON item_master(supp_id);