ALTER TABLE voucher_type_master
  ADD COLUMN alias                  VARCHAR(50)   DEFAULT NULL    AFTER voucher_type_name,
  ADD COLUMN set_alter_numbering    VARCHAR(10)   DEFAULT 'No'    AFTER abbreviation,
  ADD COLUMN default_jurisdiction   VARCHAR(100)  DEFAULT NULL    AFTER set_alter_numbering,
  ADD COLUMN default_title_to_print VARCHAR(200)  DEFAULT NULL    AFTER default_jurisdiction,
  ADD COLUMN set_alter_declaration  TINYINT(1)    DEFAULT 0       AFTER default_title_to_print,
  ADD COLUMN enable_default_alloc   TINYINT(1)    DEFAULT 0       AFTER set_alter_declaration,
  ADD COLUMN whatsapp_after_saving  TINYINT(1)    DEFAULT 0       AFTER enable_default_alloc;
