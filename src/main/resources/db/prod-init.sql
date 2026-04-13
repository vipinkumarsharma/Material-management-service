-- =====================================================
-- MMS Production DB — Consolidated Schema
-- Final state after all migrations V1–V31
-- Run against an empty schema (no Flyway history needed)
-- =====================================================

-- =====================================================
-- 1. COMPANY MASTER
-- =====================================================


CREATE TABLE `approval_rule`
(
    `rule_id`         varchar(20)    NOT NULL,
    `txn_type`        varchar(20)    NOT NULL COMMENT 'GRN / ISSUE / TRANSFER',
    `condition_type`  varchar(30)    NOT NULL COMMENT 'PRICE_VARIANCE / QTY_VARIANCE',
    `threshold_value` decimal(15, 4) NOT NULL,
    `required_role`   varchar(20)    NOT NULL,
    `created_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`rule_id`),
    KEY               `fk_approval_role` (`required_role`),
    CONSTRAINT `fk_approval_role` FOREIGN KEY (`required_role`) REFERENCES `role_master` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `branch_department_map`
(
    `branch_id`  varchar(20) NOT NULL,
    `dept_id`    int         NOT NULL,
    `created_at` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`branch_id`, `dept_id`),
    KEY          `fk_bdm_dept` (`dept_id`),
    CONSTRAINT `fk_bdm_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_bdm_dept` FOREIGN KEY (`dept_id`) REFERENCES `department_master` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `branch_item_price`
(
    `branch_id`  varchar(20)    NOT NULL,
    `item_id`    varchar(20)    NOT NULL,
    `cost_price` decimal(15, 4) NOT NULL,
    `mrp`        decimal(15, 4) NOT NULL,
    `created_at` timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`branch_id`, `item_id`),
    KEY          `fk_bip_item` (`item_id`),
    CONSTRAINT `fk_bip_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_bip_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `branch_master`
(
    `branch_id`   varchar(20)  NOT NULL,
    `branch_name` varchar(100) NOT NULL,
    `address_1`   varchar(255) DEFAULT NULL,
    `gst_no`      varchar(20)  DEFAULT NULL,
    `pincode`     varchar(10)  DEFAULT NULL,
    `created_at`  timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `company_id`  varchar(20)  DEFAULT NULL,
    PRIMARY KEY (`branch_id`),
    KEY           `idx_branch_company` (`company_id`),
    CONSTRAINT `fk_branch_company` FOREIGN KEY (`company_id`) REFERENCES `company_master` (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `branch_material_stock`
(
    `branch_id`    varchar(20)    NOT NULL,
    `item_id`      varchar(20)    NOT NULL,
    `location_id`  varchar(20)    NOT NULL,
    `qty_on_hand`  decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `avg_cost`     decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `last_updated` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`branch_id`, `item_id`, `location_id`),
    KEY            `idx_bms_item` (`item_id`),
    KEY            `idx_bms_location` (`location_id`),
    CONSTRAINT `fk_bms_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_bms_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


CREATE TABLE `company_master`
(
    `company_id`   varchar(20)  NOT NULL,
    `company_name` varchar(100) NOT NULL,
    `created_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`company_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `department_master`
(
    `dept_id`    int          NOT NULL AUTO_INCREMENT,
    `dept_name`  varchar(100) NOT NULL,
    `created_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.dept_transfer_detail definition

CREATE TABLE `dept_transfer_detail`
(
    `dept_transfer_id` bigint         NOT NULL,
    `item_id`          varchar(20)    NOT NULL,
    `location_id`      varchar(20)    NOT NULL,
    `unit_id`          varchar(20)             DEFAULT NULL,
    `qty_transferred`  decimal(15, 4) NOT NULL,
    `rate`             decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `gross_amount`     decimal(15, 4)          DEFAULT NULL,
    `discount_perc`    decimal(5, 2)  NOT NULL DEFAULT '0.00',
    `discount_amount`  decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `gst_perc`         decimal(5, 2)  NOT NULL DEFAULT '0.00',
    `gst_amount`       decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `cess_perc`        decimal(5, 2)  NOT NULL DEFAULT '0.00',
    `cess_amount`      decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `net_amount`       decimal(15, 4)          DEFAULT NULL,
    `created_at`       timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       timestamp      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`dept_transfer_id`, `item_id`),
    KEY                `fk_dtd_item` (`item_id`),
    KEY                `fk_dtd_location` (`location_id`),
    CONSTRAINT `fk_dtd_header` FOREIGN KEY (`dept_transfer_id`) REFERENCES `dept_transfer_header` (`dept_transfer_id`),
    CONSTRAINT `fk_dtd_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.dept_transfer_header definition

CREATE TABLE `dept_transfer_header`
(
    `dept_transfer_id`        bigint      NOT NULL AUTO_INCREMENT,
    `voucher_number`          varchar(50)          DEFAULT NULL,
    `voucher_type_id`         varchar(20)          DEFAULT NULL,
    `from_branch_id`          varchar(20) NOT NULL,
    `to_branch_id`            varchar(20) NOT NULL,
    `from_dept_id`            int         NOT NULL,
    `to_dept_id`              int                  DEFAULT NULL,
    `transfer_date`           date        NOT NULL,
    `effective_date`          date                 DEFAULT NULL,
    `status`                  varchar(20) NOT NULL DEFAULT 'POSTED',
    `transfer_category`       varchar(50)          DEFAULT NULL,
    `transfer_type`           varchar(20)          DEFAULT NULL,
    `transfer_out_id`         bigint               DEFAULT NULL,
    `is_received`             tinyint(1) NOT NULL DEFAULT '0',
    `transfer_mode`           varchar(50)          DEFAULT NULL,
    `remarks`                 text,
    `created_by`              varchar(50)          DEFAULT NULL,
    `items_count`             decimal(15, 4)       DEFAULT NULL,
    `total_qty`               decimal(15, 4)       DEFAULT NULL,
    `gross_amount`            decimal(15, 4)       DEFAULT NULL,
    `net_amount`              decimal(15, 4)       DEFAULT NULL,
    `round_off_amount`        decimal(15, 4)       DEFAULT '0.0000',
    `rounding_type`           varchar(20)          DEFAULT NULL,
    `third_party_supplier_id` varchar(20)          DEFAULT NULL,
    `created_at`              timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`              timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`dept_transfer_id`),
    KEY                       `fk_dth_from_dept` (`from_dept_id`),
    KEY                       `fk_dth_to_dept` (`to_dept_id`),
    KEY                       `fk_dth_from_branch` (`from_branch_id`),
    KEY                       `fk_dth_to_branch` (`to_branch_id`),
    CONSTRAINT `fk_dth_from_branch` FOREIGN KEY (`from_branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_dth_from_dept` FOREIGN KEY (`from_dept_id`) REFERENCES `department_master` (`dept_id`),
    CONSTRAINT `fk_dth_to_branch` FOREIGN KEY (`to_branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_dth_to_dept` FOREIGN KEY (`to_dept_id`) REFERENCES `department_master` (`dept_id`)
) ENGINE=InnoDB AUTO_INCREMENT=28 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.grn_detail definition

CREATE TABLE `grn_detail`
(
    `grn_id`          bigint         NOT NULL,
    `item_id`         varchar(20)    NOT NULL,
    `unit_id`         varchar(20)    NOT NULL,
    `location_id`     varchar(20)    NOT NULL,
    `qty_received`    decimal(15, 4) NOT NULL,
    `rate`            decimal(15, 4) NOT NULL,
    `gst_perc`        decimal(5, 2)           DEFAULT '0.00',
    `gst_amount`      decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `discount_perc`   decimal(5, 2)  NOT NULL DEFAULT '0.00',
    `discount_amount` decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `cess_perc`       decimal(5, 2)  NOT NULL DEFAULT '0.00',
    `cess_amount`     decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `net_amount`      decimal(15, 4)          DEFAULT NULL,
    `gross_amount`    decimal(15, 4) NOT NULL,
    `qty_remaining`   decimal(15, 4) NOT NULL COMMENT 'For FIFO tracking - remaining stock from this GRN',
    `created_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`grn_id`, `item_id`),
    KEY               `fk_grnd_unit` (`unit_id`),
    KEY               `idx_grnd_item` (`item_id`),
    KEY               `idx_grnd_location` (`location_id`),
    CONSTRAINT `fk_grnd_grn` FOREIGN KEY (`grn_id`) REFERENCES `grn_header` (`grn_id`),
    CONSTRAINT `fk_grnd_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`),
    CONSTRAINT `fk_grnd_unit` FOREIGN KEY (`unit_id`) REFERENCES `unit_master` (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.grn_header definition

CREATE TABLE `grn_header`
(
    `grn_id`           bigint      NOT NULL AUTO_INCREMENT,
    `voucher_number`   varchar(50)          DEFAULT NULL,
    `voucher_type_id`  varchar(20)          DEFAULT NULL,
    `branch_id`        varchar(20) NOT NULL,
    `dept_id`          int                  DEFAULT NULL,
    `supp_id`          varchar(20) NOT NULL,
    `invoice_id`       bigint               DEFAULT NULL,
    `challan_no`       varchar(50)          DEFAULT NULL,
    `challan_date`     date                 DEFAULT NULL,
    `invoice_date`     date                 DEFAULT NULL,
    `grn_date`         date        NOT NULL,
    `effective_date`   date                 DEFAULT NULL,
    `status`           varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PENDING_APPROVAL / POSTED',
    `remarks`          text,
    `items_count`      decimal(15, 4)       DEFAULT NULL,
    `total_qty`        decimal(15, 4)       DEFAULT NULL,
    `gross_amount`     decimal(15, 4)       DEFAULT NULL,
    `net_amount`       decimal(15, 4)       DEFAULT NULL,
    `round_off_amount` decimal(15, 4)       DEFAULT '0.0000',
    `created_by`       varchar(50)          DEFAULT NULL,
    `approved_by`      varchar(50)          DEFAULT NULL,
    `approved_at`      timestamp NULL DEFAULT NULL,
    `created_at`       timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `pv_id`            bigint               DEFAULT NULL,
    PRIMARY KEY (`grn_id`),
    KEY                `fk_grn_invoice` (`invoice_id`),
    KEY                `idx_grn_branch` (`branch_id`),
    KEY                `idx_grn_supplier` (`supp_id`),
    KEY                `idx_grn_date` (`grn_date`),
    KEY                `idx_grn_status` (`status`),
    KEY                `fk_grn_dept` (`dept_id`),
    KEY                `fk_grn_pv` (`pv_id`),
    CONSTRAINT `fk_grn_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_grn_dept` FOREIGN KEY (`dept_id`) REFERENCES `department_master` (`dept_id`),
    CONSTRAINT `fk_grn_pv` FOREIGN KEY (`pv_id`) REFERENCES `purchase_voucher_header` (`pv_id`),
    CONSTRAINT `fk_grn_supplier` FOREIGN KEY (`supp_id`) REFERENCES `supplier_master` (`supp_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.group_master definition

CREATE TABLE `group_master`
(
    `group_id`   varchar(20)  NOT NULL,
    `group_desc` varchar(100) NOT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.issue_detail definition

CREATE TABLE `issue_detail`
(
    `issue_id`    bigint         NOT NULL,
    `item_id`     varchar(20)    NOT NULL,
    `location_id` varchar(20)    NOT NULL,
    `qty_issued`  decimal(15, 4) NOT NULL,
    `rate`        decimal(15, 4) NOT NULL COMMENT 'Derived from FIFO GRN rate',
    `created_at`  timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`issue_id`, `item_id`),
    KEY           `idx_issued_item` (`item_id`),
    KEY           `idx_issued_location` (`location_id`),
    CONSTRAINT `fk_issued_issue` FOREIGN KEY (`issue_id`) REFERENCES `issue_header` (`issue_id`),
    CONSTRAINT `fk_issued_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`),
    CONSTRAINT `fk_issued_location` FOREIGN KEY (`location_id`) REFERENCES `location_master` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.issue_fifo_consumption definition

CREATE TABLE `issue_fifo_consumption`
(
    `consumption_id` bigint         NOT NULL AUTO_INCREMENT,
    `issue_id`       bigint         NOT NULL,
    `item_id`        varchar(20)    NOT NULL,
    `grn_id`         bigint         NOT NULL,
    `qty_consumed`   decimal(15, 4) NOT NULL,
    `rate`           decimal(15, 4) NOT NULL,
    `created_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`consumption_id`),
    KEY              `fk_fifo_issue` (`issue_id`,`item_id`),
    KEY              `idx_fifo_grn` (`grn_id`,`item_id`),
    CONSTRAINT `fk_fifo_grn` FOREIGN KEY (`grn_id`, `item_id`) REFERENCES `grn_detail` (`grn_id`, `item_id`),
    CONSTRAINT `fk_fifo_issue` FOREIGN KEY (`issue_id`, `item_id`) REFERENCES `issue_detail` (`issue_id`, `item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.issue_header definition

CREATE TABLE `issue_header`
(
    `issue_id`             bigint       NOT NULL AUTO_INCREMENT,
    `voucher_number`       varchar(50)           DEFAULT NULL,
    `voucher_type_id`      varchar(20)           DEFAULT NULL,
    `branch_id`            varchar(20)  NOT NULL,
    `dept_id`              int                   DEFAULT NULL,
    `issue_date`           date         NOT NULL,
    `effective_date`       date                  DEFAULT NULL,
    `issued_to`            varchar(100) NOT NULL,
    `status`               varchar(20)  NOT NULL DEFAULT 'DRAFT' COMMENT 'DRAFT / PENDING_APPROVAL / POSTED',
    `issue_type`           varchar(20)  NOT NULL DEFAULT 'REGULAR',
    `supp_id`              varchar(20)           DEFAULT NULL,
    `expected_return_date` date                  DEFAULT NULL,
    `remarks`              text,
    `created_by`           varchar(50)           DEFAULT NULL,
    `approved_by`          varchar(50)           DEFAULT NULL,
    `approved_at`          timestamp NULL DEFAULT NULL,
    `created_at`           timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`issue_id`),
    KEY                    `idx_issue_branch` (`branch_id`),
    KEY                    `idx_issue_date` (`issue_date`),
    KEY                    `idx_issue_status` (`status`),
    KEY                    `fk_issue_dept` (`dept_id`),
    KEY                    `fk_issue_supplier` (`supp_id`),
    CONSTRAINT `fk_issue_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_issue_dept` FOREIGN KEY (`dept_id`) REFERENCES `department_master` (`dept_id`),
    CONSTRAINT `fk_issue_supplier` FOREIGN KEY (`supp_id`) REFERENCES `supplier_master` (`supp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.item_master definition

CREATE TABLE `item_master`
(
    `item_id`      varchar(20)  NOT NULL,
    `item_desc`    varchar(200) NOT NULL,
    `group_id`     varchar(20)                                                  DEFAULT NULL,
    `sub_group_id` varchar(20)                                                  DEFAULT NULL,
    `manuf_id`     varchar(20)                                                  DEFAULT NULL,
    `supp_id`      varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
    `unit_id`      varchar(20)                                                  DEFAULT NULL,
    `gst_perc`     decimal(5, 2)                                                DEFAULT '0.00',
    `cost_price`   decimal(15, 4)                                               DEFAULT '0.0000',
    `mrp`          decimal(15, 4)                                               DEFAULT '0.0000',
    `hsn_code`     varchar(20)                                                  DEFAULT NULL,
    `cess_perc`    decimal(5, 2)                                                DEFAULT '0.00',
    `created_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `company_id`   varchar(20)                                                  DEFAULT NULL,
    PRIMARY KEY (`item_id`),
    KEY            `fk_item_unit` (`unit_id`),
    KEY            `idx_item_group` (`group_id`,`sub_group_id`),
    KEY            `idx_item_company` (`company_id`),
    CONSTRAINT `fk_item_company` FOREIGN KEY (`company_id`) REFERENCES `company_master` (`company_id`),
    CONSTRAINT `fk_item_group` FOREIGN KEY (`group_id`, `sub_group_id`) REFERENCES `sub_group_master` (`group_id`, `sub_group_id`),
    CONSTRAINT `fk_item_unit` FOREIGN KEY (`unit_id`) REFERENCES `unit_master` (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.location_master definition

CREATE TABLE `location_master`
(
    `location_id`   varchar(20)  NOT NULL,
    `branch_id`     varchar(20)  NOT NULL,
    `location_name` varchar(100) NOT NULL,
    `parent_id`     varchar(20) DEFAULT NULL,
    `created_at`    timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`    timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`location_id`),
    KEY             `fk_location_parent` (`parent_id`),
    KEY             `idx_location_branch` (`branch_id`),
    CONSTRAINT `fk_location_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.material_stock_ledger definition

CREATE TABLE `material_stock_ledger`
(
    `ledger_id`   bigint         NOT NULL AUTO_INCREMENT,
    `branch_id`   varchar(20)    NOT NULL,
    `item_id`     varchar(20)    NOT NULL,
    `location_id` varchar(20)    NOT NULL,
    `dept_id`     int                     DEFAULT NULL,
    `txn_date`    date           NOT NULL,
    `txn_type`    varchar(30)    NOT NULL,
    `ref_id`      bigint                  DEFAULT NULL,
    `qty_in`      decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `qty_out`     decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `rate`        decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `balance_qty` decimal(15, 4) NOT NULL,
    `remarks`     varchar(255)            DEFAULT NULL,
    `created_on`  timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`ledger_id`),
    KEY           `fk_ledger_item` (`item_id`),
    KEY           `idx_ledger_branch_item` (`branch_id`,`item_id`),
    KEY           `idx_ledger_location` (`location_id`),
    KEY           `idx_ledger_txn_date` (`txn_date`),
    KEY           `idx_ledger_txn_type` (`txn_type`),
    KEY           `idx_ledger_ref` (`txn_type`,`ref_id`),
    KEY           `fk_ledger_dept` (`dept_id`),
    CONSTRAINT `fk_ledger_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_ledger_dept` FOREIGN KEY (`dept_id`) REFERENCES `department_master` (`dept_id`),
    CONSTRAINT `fk_ledger_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.po_detail definition

CREATE TABLE `po_detail`
(
    `po_id`        bigint         NOT NULL,
    `item_id`      varchar(20)    NOT NULL,
    `qty_ordered`  decimal(15, 4) NOT NULL,
    `rate`         decimal(15, 4) NOT NULL,
    `qty_received` decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `created_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`po_id`, `item_id`),
    KEY            `fk_pod_item` (`item_id`),
    CONSTRAINT `fk_pod_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`),
    CONSTRAINT `fk_pod_po` FOREIGN KEY (`po_id`) REFERENCES `po_header` (`po_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.po_header definition

CREATE TABLE `po_header`
(
    `po_id`           bigint      NOT NULL AUTO_INCREMENT,
    `voucher_number`  varchar(50)          DEFAULT NULL,
    `voucher_type_id` varchar(20)          DEFAULT NULL,
    `branch_id`       varchar(20) NOT NULL,
    `supp_id`         varchar(20) NOT NULL,
    `po_date`         date        NOT NULL,
    `effective_date`  date                 DEFAULT NULL,
    `status`          varchar(20) NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN / PARTIAL / CLOSED',
    `created_by`      varchar(50)          DEFAULT NULL,
    `created_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`po_id`),
    KEY               `idx_po_branch` (`branch_id`),
    KEY               `idx_po_supplier` (`supp_id`),
    KEY               `idx_po_date` (`po_date`),
    CONSTRAINT `fk_po_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_po_supplier` FOREIGN KEY (`supp_id`) REFERENCES `supplier_master` (`supp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.purchase_voucher_detail definition

CREATE TABLE `purchase_voucher_detail`
(
    `pv_id`           bigint      NOT NULL,
    `item_id`         varchar(20) NOT NULL,
    `unit_id`         varchar(20)    DEFAULT NULL,
    `location_id`     varchar(20)    DEFAULT NULL,
    `qty`             decimal(15, 4) DEFAULT '0.0000',
    `rate`            decimal(15, 4) DEFAULT '0.0000',
    `gross_amount`    decimal(15, 2) DEFAULT '0.00',
    `discount_perc`   decimal(5, 2)  DEFAULT '0.00',
    `discount_amount` decimal(15, 2) DEFAULT '0.00',
    `gst_perc`        decimal(5, 2)  DEFAULT '0.00',
    `gst_amount`      decimal(15, 2) DEFAULT '0.00',
    `cess_perc`       decimal(5, 2)  DEFAULT '0.00',
    `cess_amount`     decimal(15, 2) DEFAULT '0.00',
    `net_amount`      decimal(15, 2) DEFAULT '0.00',
    `line_narration`  text,
    PRIMARY KEY (`pv_id`, `item_id`),
    KEY               `fk_pvd_item` (`item_id`),
    KEY               `fk_pvd_unit` (`unit_id`),
    KEY               `fk_pvd_location` (`location_id`),
    CONSTRAINT `fk_pvd_header` FOREIGN KEY (`pv_id`) REFERENCES `purchase_voucher_header` (`pv_id`),
    CONSTRAINT `fk_pvd_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`),
    CONSTRAINT `fk_pvd_location` FOREIGN KEY (`location_id`) REFERENCES `location_master` (`location_id`),
    CONSTRAINT `fk_pvd_unit` FOREIGN KEY (`unit_id`) REFERENCES `unit_master` (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.purchase_voucher_header definition

CREATE TABLE `purchase_voucher_header`
(
    `pv_id`                 bigint      NOT NULL AUTO_INCREMENT,
    `voucher_number`        varchar(50)          DEFAULT NULL,
    `voucher_type_id`       varchar(20)          DEFAULT NULL,
    `branch_id`             varchar(20) NOT NULL,
    `supp_id`               varchar(20) NOT NULL,
    `pv_date`               date        NOT NULL,
    `effective_date`        date                 DEFAULT NULL,
    `grn_id`                bigint               DEFAULT NULL,
    `invoice_id`            bigint               DEFAULT NULL,
    `supplier_invoice_no`   varchar(100)         DEFAULT NULL,
    `supplier_invoice_date` date                 DEFAULT NULL,
    `status`                varchar(20) NOT NULL DEFAULT 'DRAFT',
    `is_optional`           tinyint(1) DEFAULT '0',
    `gross_amount`          decimal(15, 2)       DEFAULT '0.00',
    `discount_amount`       decimal(15, 2)       DEFAULT '0.00',
    `gst_amount`            decimal(15, 2)       DEFAULT '0.00',
    `cess_amount`           decimal(15, 2)       DEFAULT '0.00',
    `net_amount`            decimal(15, 2)       DEFAULT '0.00',
    `round_off_amount`      decimal(15, 2)       DEFAULT '0.00',
    `narration`             text,
    `created_by`            varchar(100)         DEFAULT NULL,
    `approved_by`           varchar(100)         DEFAULT NULL,
    `approved_at`           datetime             DEFAULT NULL,
    `created_at`            timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`            timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `mode_of_payment`       varchar(100)         DEFAULT NULL,
    `other_references`      varchar(255)         DEFAULT NULL,
    `terms_of_delivery`     varchar(255)         DEFAULT NULL,
    `dispatch_through`      varchar(255)         DEFAULT NULL,
    `destination`           varchar(255)         DEFAULT NULL,
    `carrier_name_agent`    varchar(255)         DEFAULT NULL,
    `bill_of_lading_no`     varchar(100)         DEFAULT NULL,
    `motor_vehicle_no`      varchar(100)         DEFAULT NULL,
    PRIMARY KEY (`pv_id`),
    KEY                     `fk_pvh_voucher_type` (`voucher_type_id`),
    KEY                     `fk_pvh_invoice` (`invoice_id`),
    KEY                     `idx_pv_branch` (`branch_id`),
    KEY                     `idx_pv_supp` (`supp_id`),
    KEY                     `idx_pv_date` (`pv_date`),
    KEY                     `idx_pv_status` (`status`),
    KEY                     `idx_pv_grn` (`grn_id`),
    CONSTRAINT `fk_pvh_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_pvh_grn` FOREIGN KEY (`grn_id`) REFERENCES `grn_header` (`grn_id`),
    CONSTRAINT `fk_pvh_invoice` FOREIGN KEY (`invoice_id`) REFERENCES `supplier_invoice` (`invoice_id`),
    CONSTRAINT `fk_pvh_supplier` FOREIGN KEY (`supp_id`) REFERENCES `supplier_master` (`supp_id`),
    CONSTRAINT `fk_pvh_voucher_type` FOREIGN KEY (`voucher_type_id`) REFERENCES `voucher_type_master` (`voucher_type_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.stock_transfer_detail definition

CREATE TABLE `stock_transfer_detail`
(
    `transfer_id`  bigint         NOT NULL,
    `item_id`      varchar(20)    NOT NULL,
    `qty_sent`     decimal(15, 4) NOT NULL,
    `rate`         decimal(15, 4) NOT NULL DEFAULT '0.0000' COMMENT 'Transfer rate from sender FIFO',
    `qty_received` decimal(15, 4)          DEFAULT '0.0000' COMMENT 'Qty received at destination',
    `created_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`   timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`transfer_id`, `item_id`),
    KEY            `idx_std_item` (`item_id`),
    CONSTRAINT `fk_std_item` FOREIGN KEY (`item_id`) REFERENCES `item_master` (`item_id`),
    CONSTRAINT `fk_std_transfer` FOREIGN KEY (`transfer_id`) REFERENCES `stock_transfer_header` (`transfer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.stock_transfer_header definition

CREATE TABLE `stock_transfer_header`
(
    `transfer_id`     bigint      NOT NULL AUTO_INCREMENT,
    `voucher_number`  varchar(50)          DEFAULT NULL,
    `voucher_type_id` varchar(20)          DEFAULT NULL,
    `from_branch`     varchar(20) NOT NULL,
    `to_branch`       varchar(20) NOT NULL,
    `dept_id`         int                  DEFAULT NULL,
    `transfer_date`   date        NOT NULL,
    `effective_date`  date                 DEFAULT NULL,
    `status`          varchar(20) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED / IN_TRANSIT / RECEIVED',
    `sender_issue_id` bigint               DEFAULT NULL COMMENT 'Issue created at sender branch',
    `receiver_grn_id` bigint               DEFAULT NULL COMMENT 'GRN created at receiver branch',
    `remarks`         text,
    `created_by`      varchar(50)          DEFAULT NULL,
    `approved_by`     varchar(50)          DEFAULT NULL,
    `approved_at`     timestamp NULL DEFAULT NULL,
    `created_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`transfer_id`),
    KEY               `idx_transfer_from` (`from_branch`),
    KEY               `idx_transfer_to` (`to_branch`),
    KEY               `idx_transfer_date` (`transfer_date`),
    KEY               `idx_transfer_status` (`status`),
    KEY               `fk_transfer_sender_issue` (`sender_issue_id`),
    KEY               `fk_transfer_receiver_grn` (`receiver_grn_id`),
    KEY               `fk_transfer_dept` (`dept_id`),
    CONSTRAINT `fk_transfer_dept` FOREIGN KEY (`dept_id`) REFERENCES `department_master` (`dept_id`),
    CONSTRAINT `fk_transfer_from_branch` FOREIGN KEY (`from_branch`) REFERENCES `branch_master` (`branch_id`),
    CONSTRAINT `fk_transfer_receiver_grn` FOREIGN KEY (`receiver_grn_id`) REFERENCES `grn_header` (`grn_id`),
    CONSTRAINT `fk_transfer_sender_issue` FOREIGN KEY (`sender_issue_id`) REFERENCES `issue_header` (`issue_id`),
    CONSTRAINT `fk_transfer_to_branch` FOREIGN KEY (`to_branch`) REFERENCES `branch_master` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.sub_group_master definition

CREATE TABLE `sub_group_master`
(
    `group_id`       varchar(20)  NOT NULL,
    `sub_group_id`   varchar(20)  NOT NULL,
    `sub_group_desc` varchar(100) NOT NULL,
    `created_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`group_id`, `sub_group_id`),
    CONSTRAINT `fk_subgroup_group` FOREIGN KEY (`group_id`) REFERENCES `group_master` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.supplier_invoice definition

CREATE TABLE `supplier_invoice`
(
    `invoice_id`     bigint         NOT NULL AUTO_INCREMENT,
    `supp_id`        varchar(20)    NOT NULL,
    `invoice_no`     varchar(50)    NOT NULL,
    `invoice_date`   date           NOT NULL,
    `invoice_amount` decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `gst_amount`     decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `net_amount`     decimal(15, 4) NOT NULL DEFAULT '0.0000',
    `invoice_s3_url` varchar(500)            DEFAULT NULL,
    `created_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`     timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`invoice_id`),
    KEY              `idx_invoice_supplier` (`supp_id`),
    KEY              `idx_invoice_date` (`invoice_date`),
    CONSTRAINT `fk_invoice_supplier` FOREIGN KEY (`supp_id`) REFERENCES `supplier_master` (`supp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.supplier_master definition

CREATE TABLE `supplier_master`
(
    `supp_id`    varchar(20)  NOT NULL,
    `supp_name`  varchar(100) NOT NULL,
    `address`    varchar(255) DEFAULT NULL,
    `mob_no`     varchar(20)  DEFAULT NULL,
    `email`      varchar(100) DEFAULT NULL,
    `gstin`      varchar(20)  DEFAULT NULL,
    `type`       varchar(50)  DEFAULT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`supp_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.unit_master definition

CREATE TABLE `unit_master`
(
    `unit_id`    varchar(20) NOT NULL,
    `unit_desc`  varchar(50) NOT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.user_role_map definition

CREATE TABLE `user_role_map`
(
    `user_id`    varchar(50) NOT NULL,
    `role_id`    varchar(20) NOT NULL,
    `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY          `fk_urm_role` (`role_id`),
    CONSTRAINT `fk_urm_role` FOREIGN KEY (`role_id`) REFERENCES `role_master` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- beejapuri_QA_MM.voucher_series_master definition

CREATE TABLE `voucher_series_master`
(
    `series_id`           varchar(20)  NOT NULL,
    `series_name`         varchar(100) NOT NULL,
    `voucher_type_id`     varchar(20)  NOT NULL,
    `branch_id`           varchar(20)  NOT NULL,
    `starting_number`     int          DEFAULT '1',
    `current_number`      int          DEFAULT '1',
    `number_width`        int          DEFAULT '6',
    `prefill_with_zero`   tinyint(1) DEFAULT '1',
    `prefix_details`      varchar(100) DEFAULT '',
    `suffix_details`      varchar(100) DEFAULT '',
    `restart_periodicity` varchar(20)  DEFAULT 'ANNUALLY',
    `last_reset_date`     date         DEFAULT NULL,
    `next_restart_date`   date         DEFAULT NULL,
    `is_default`          tinyint(1) DEFAULT '0',
    `is_active`           tinyint(1) DEFAULT '1',
    `created_at`          timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`          timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`series_id`),
    UNIQUE KEY `uq_series_default` (`voucher_type_id`,`branch_id`,`is_default`),
    KEY                   `fk_vsm_branch` (`branch_id`),
    CONSTRAINT `fk_vsm_voucher_type` FOREIGN KEY (`voucher_type_id`) REFERENCES `voucher_type_master` (`voucher_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.voucher_series_restart_schedule definition

CREATE TABLE `voucher_series_restart_schedule`
(
    `restart_id`           bigint      NOT NULL AUTO_INCREMENT,
    `series_id`            varchar(20) NOT NULL,
    `applicable_from_date` date        NOT NULL,
    `starting_number`      int          DEFAULT '1',
    `prefix_override`      varchar(100) DEFAULT NULL,
    `suffix_override`      varchar(100) DEFAULT NULL,
    `created_at`           timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`restart_id`),
    KEY                    `idx_restart_series` (`series_id`,`applicable_from_date`),
    CONSTRAINT `fk_vsrs_series` FOREIGN KEY (`series_id`) REFERENCES `voucher_series_master` (`series_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- beejapuri_QA_MM.voucher_type_master definition

CREATE TABLE `voucher_type_master`
(
    `voucher_type_id`        varchar(20)  NOT NULL,
    `branch_id`              varchar(20)    DEFAULT NULL,
    `voucher_type_name`      varchar(100) NOT NULL,
    `alias`                  varchar(50)    DEFAULT NULL,
    `base_txn_type`          varchar(30)  NOT NULL,
    `abbreviation`           varchar(10)    DEFAULT NULL,
    `set_alter_numbering`    varchar(10)    DEFAULT 'No',
    `default_jurisdiction`   varchar(100)   DEFAULT NULL,
    `default_title_to_print` varchar(200)   DEFAULT NULL,
    `set_alter_declaration`  tinyint(1) DEFAULT '0',
    `enable_default_alloc`   tinyint(1) DEFAULT '0',
    `whatsapp_after_saving`  tinyint(1) DEFAULT '0',
    `is_active`              tinyint(1) DEFAULT '1',
    `numbering_method`       varchar(30)    DEFAULT 'AUTOMATIC',
    `numbering_on_deletion`  varchar(30)    DEFAULT 'RETAIN_ORIGINAL',
    `show_unused_numbers`    tinyint(1) DEFAULT '0',
    `prevent_duplicates`     tinyint(1) DEFAULT '1',
    `allow_zero_valued`      tinyint(1) DEFAULT '0',
    `is_optional_default`    tinyint(1) DEFAULT '0',
    `use_effective_dates`    tinyint(1) DEFAULT '0',
    `effective_date_label`   varchar(50)    DEFAULT 'Effective Date',
    `allow_narration`        tinyint(1) DEFAULT '1',
    `narration_mandatory`    tinyint(1) DEFAULT '0',
    `narration_per_line`     tinyint(1) DEFAULT '0',
    `print_after_save`       tinyint(1) DEFAULT '0',
    `require_approval`       tinyint(1) DEFAULT '0',
    `approval_amount_limit`  decimal(15, 2) DEFAULT NULL,
    `created_at`             timestamp NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`voucher_type_id`),
    KEY                      `fk_vt_branch` (`branch_id`),
    CONSTRAINT `fk_vt_branch` FOREIGN KEY (`branch_id`) REFERENCES `branch_master` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


--indexes

CREATE INDEX idx_branch_company ON branch_master (company_id);

-- =====================================================
-- 3. GROUP MASTER  (V1)

CREATE INDEX idx_item_group ON item_master (group_id, sub_group_id);
CREATE INDEX idx_item_supp ON item_master (supp_id);
CREATE INDEX idx_item_company ON item_master (company_id);


CREATE INDEX idx_location_branch ON location_master (branch_id);


CREATE INDEX idx_bdm_dept ON branch_department_map (dept_id);


CREATE INDEX idx_bip_item ON branch_item_price (item_id);


CREATE INDEX idx_invoice_supplier ON supplier_invoice (supp_id);
CREATE INDEX idx_invoice_date ON supplier_invoice (invoice_date);


CREATE INDEX idx_po_branch ON po_header (branch_id);
CREATE INDEX idx_po_supplier ON po_header (supp_id);
CREATE INDEX idx_po_date ON po_header (po_date);


CREATE INDEX idx_grn_branch ON grn_header (branch_id);
CREATE INDEX idx_grn_supplier ON grn_header (supp_id);
CREATE INDEX idx_grn_date ON grn_header (grn_date);
CREATE INDEX idx_grn_status ON grn_header (status);
CREATE INDEX idx_grn_dept ON grn_header (dept_id);

CREATE INDEX idx_grnd_item ON grn_detail (item_id);


ALTER TABLE grn_header
    ADD CONSTRAINT fk_grn_pv FOREIGN KEY (pv_id) REFERENCES purchase_voucher_header (pv_id);

CREATE INDEX idx_grn_pv ON grn_header (pv_id);


CREATE INDEX idx_issue_branch ON issue_header (branch_id);
CREATE INDEX idx_issue_date ON issue_header (issue_date);
CREATE INDEX idx_issue_status ON issue_header (status);
CREATE INDEX idx_issue_dept ON issue_header (dept_id);
CREATE INDEX idx_issue_type ON issue_header (issue_type);
CREATE INDEX idx_issue_supp ON issue_header (supp_id);


CREATE INDEX idx_issued_item ON issue_detail (item_id);
CREATE INDEX idx_issued_location ON issue_detail (location_id);


CREATE INDEX idx_fifo_grn ON issue_fifo_consumption (grn_id, item_id);


CREATE INDEX idx_transfer_from ON stock_transfer_header (from_branch);
CREATE INDEX idx_transfer_to ON stock_transfer_header (to_branch);
CREATE INDEX idx_transfer_date ON stock_transfer_header (transfer_date);
CREATE INDEX idx_transfer_status ON stock_transfer_header (status);
CREATE INDEX idx_transfer_dept ON stock_transfer_header (dept_id);


CREATE INDEX idx_std_item ON stock_transfer_detail (item_id);

-- =====================================================
-- 31. MATERIAL STOCK LEDGER  (V6 + V7 dept + V8 nullable ref_id + V9 widen + remarks)
-- =====================================================


CREATE INDEX idx_ledger_branch_item ON material_stock_ledger (branch_id, item_id);
CREATE INDEX idx_ledger_location ON material_stock_ledger (location_id);
CREATE INDEX idx_ledger_txn_date ON material_stock_ledger (txn_date);
CREATE INDEX idx_ledger_txn_type ON material_stock_ledger (txn_type);
CREATE INDEX idx_ledger_ref ON material_stock_ledger (txn_type, ref_id);
CREATE INDEX idx_ledger_dept ON material_stock_ledger (dept_id);


CREATE INDEX idx_bms_item ON branch_material_stock (item_id);
CREATE INDEX idx_bms_location ON branch_material_stock (location_id);

CREATE INDEX idx_dth_from_branch ON dept_transfer_header (from_branch_id);
CREATE INDEX idx_dth_to_branch ON dept_transfer_header (to_branch_id);
CREATE INDEX idx_dth_from_dept ON dept_transfer_header (from_dept_id);
CREATE INDEX idx_dth_to_dept ON dept_transfer_header (to_dept_id);
CREATE INDEX idx_dth_date ON dept_transfer_header (transfer_date);
CREATE INDEX idx_dtd_item ON dept_transfer_detail (item_id);
CREATE INDEX idx_dtd_location ON dept_transfer_detail (location_id);

