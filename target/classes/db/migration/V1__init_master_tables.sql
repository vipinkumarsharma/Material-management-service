-- =====================================================
-- Material Management System - Database Schema
-- V1: Master Tables
-- =====================================================

-- Branch Master
CREATE TABLE branch_master (
    branch_id VARCHAR(20) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    gst_no VARCHAR(20),
    pincode VARCHAR(10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Group Master
CREATE TABLE group_master (
    group_id VARCHAR(20) NOT NULL,
    group_desc VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Sub Group Master
CREATE TABLE sub_group_master (
    group_id VARCHAR(20) NOT NULL,
    sub_group_id VARCHAR(20) NOT NULL,
    sub_group_desc VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, sub_group_id),
    CONSTRAINT fk_subgroup_group FOREIGN KEY (group_id) REFERENCES group_master(group_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Unit Master
CREATE TABLE unit_master (
    unit_id VARCHAR(20) NOT NULL,
    unit_desc VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Manufacturer Master
CREATE TABLE manufacturer_master (
    manuf_id VARCHAR(20) NOT NULL,
    manuf_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    address_3 VARCHAR(255),
    phone_1 VARCHAR(20),
    phone_2 VARCHAR(20),
    email VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (manuf_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Supplier Master
CREATE TABLE supplier_master (
    supp_id VARCHAR(20) NOT NULL,
    supp_name VARCHAR(100) NOT NULL,
    address_1 VARCHAR(255),
    address_2 VARCHAR(255),
    address_3 VARCHAR(255),
    mob_no VARCHAR(20),
    phone_1 VARCHAR(20),
    email VARCHAR(100),
    agency VARCHAR(100),
    cont_pers VARCHAR(100),
    gstin VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (supp_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Item Master
CREATE TABLE item_master (
    item_id VARCHAR(20) NOT NULL,
    item_desc VARCHAR(200) NOT NULL,
    group_id VARCHAR(20),
    sub_group_id VARCHAR(20),
    manuf_id VARCHAR(20),
    unit_id VARCHAR(20),
    gst_perc DECIMAL(5,2) DEFAULT 0,
    cost_price DECIMAL(15,4) DEFAULT 0,
    mrp DECIMAL(15,4) DEFAULT 0,
    hsn_code VARCHAR(20),
    cess_perc DECIMAL(5,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (item_id),
    CONSTRAINT fk_item_group FOREIGN KEY (group_id, sub_group_id) REFERENCES sub_group_master(group_id, sub_group_id),
    CONSTRAINT fk_item_manuf FOREIGN KEY (manuf_id) REFERENCES manufacturer_master(manuf_id),
    CONSTRAINT fk_item_unit FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Location Master
CREATE TABLE location_master (
    location_id VARCHAR(20) NOT NULL,
    branch_id VARCHAR(20) NOT NULL,
    location_name VARCHAR(100) NOT NULL,
    parent_id VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (location_id),
    CONSTRAINT fk_location_branch FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
    CONSTRAINT fk_location_parent FOREIGN KEY (parent_id) REFERENCES location_master(location_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Role Master
CREATE TABLE role_master (
    role_id VARCHAR(20) NOT NULL,
    role_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- User Role Map
CREATE TABLE user_role_map (
    user_id VARCHAR(50) NOT NULL,
    role_id VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_urm_role FOREIGN KEY (role_id) REFERENCES role_master(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Approval Rule
CREATE TABLE approval_rule (
    rule_id VARCHAR(20) NOT NULL,
    txn_type VARCHAR(20) NOT NULL COMMENT 'GRN / ISSUE / TRANSFER',
    condition_type VARCHAR(30) NOT NULL COMMENT 'PRICE_VARIANCE / QTY_VARIANCE',
    threshold_value DECIMAL(15,4) NOT NULL,
    required_role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (rule_id),
    CONSTRAINT fk_approval_role FOREIGN KEY (required_role) REFERENCES role_master(role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for Master Tables
CREATE INDEX idx_item_group ON item_master(group_id, sub_group_id);
CREATE INDEX idx_item_manuf ON item_master(manuf_id);
CREATE INDEX idx_location_branch ON location_master(branch_id);
