ALTER TABLE supplier_master
    CHANGE COLUMN address_1 address VARCHAR(255),
    DROP COLUMN address_2,
    DROP COLUMN address_3,
    DROP COLUMN phone_1,
    DROP COLUMN agency,
    DROP COLUMN cont_pers;
