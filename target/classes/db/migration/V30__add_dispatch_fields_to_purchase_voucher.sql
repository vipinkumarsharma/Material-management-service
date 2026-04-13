ALTER TABLE purchase_voucher_header
    ADD COLUMN mode_of_payment     VARCHAR(100) NULL,
    ADD COLUMN other_references    VARCHAR(255) NULL,
    ADD COLUMN terms_of_delivery   VARCHAR(255) NULL,
    ADD COLUMN dispatch_through    VARCHAR(255) NULL,
    ADD COLUMN destination         VARCHAR(255) NULL,
    ADD COLUMN carrier_name_agent  VARCHAR(255) NULL,
    ADD COLUMN bill_of_lading_no   VARCHAR(100) NULL,
    ADD COLUMN motor_vehicle_no    VARCHAR(100) NULL;
