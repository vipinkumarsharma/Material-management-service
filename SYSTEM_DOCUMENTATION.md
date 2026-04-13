# Material Management System (MMS)
## Complete System Documentation

---

## 📋 Table of Contents
1. [System Overview](#system-overview)
2. [Architecture](#architecture)
3. [Tech Stack](#tech-stack)
4. [Getting Started](#getting-started)
5. [Core Modules](#core-modules)
6. [API Documentation](#api-documentation)
7. [Database Schema](#database-schema)
8. [Business Logic Patterns](#business-logic-patterns)
9. [Key Workflows](#key-workflows)
10. [Reporting](#reporting)

---

## 🎯 System Overview

The Material Management System (MMS) is an enterprise-grade inventory management solution built for multi-branch operations. It provides comprehensive features for:

- **Inventory Control**: Real-time tracking of materials across multiple warehouses
- **FIFO Compliance**: Automated First-In-First-Out batch consumption
- **Multi-Branch Operations**: Inter-branch stock transfers with complete traceability
- **Approval Workflows**: Price variance detection and multi-level approvals
- **Audit Trail**: Append-only ledger system for complete transaction history
- **Comprehensive Reporting**: 14+ built-in reports for inventory analytics

### Key Capabilities

✅ Goods Receipt Notes (GRN) with PO matching
✅ Material Issuance with automatic FIFO consumption
✅ Inter-branch stock transfers (3-stage workflow)
✅ Stock aging analysis (0-30, 31-60, 61-90, 90+ days)
✅ Price variance alerts and approval routing
✅ Supplier invoice management with PDF storage (AWS S3)
✅ Real-time stock balance queries
✅ Multi-branch consolidated reporting

---

## 🏗 Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   REST API Layer                         │
│        (Spring Boot Controllers - 12 controllers)        │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                Service Layer                             │
│  (Business Logic - 18 services)                          │
│  - Master Services (9)                                   │
│  - GrnService, IssueService, TransferService            │
│  - FifoService, ApprovalService                         │
│  - StockLedgerService, AgingService, ReportService      │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│              Repository Layer                            │
│     (Spring Data JPA - 24 repositories)                  │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                Database Layer                            │
│  MySQL 8.0 with Flyway Migrations (V1-V6)               │
│  - 11 Master Tables                                      │
│  - Transactional Tables (GRN, Issue, Transfer)          │
│  - Stock Ledger (Append-Only)                           │
└──────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────┐
│              External Integrations                        │
│  - AWS S3 (Invoice PDF Storage)                          │
└──────────────────────────────────────────────────────────┘
```

### Project Structure

```
src/main/java/com/countrydelight/mms/
├── config/                       # AWS S3 configuration
│   └── S3Config.java
│
├── controller/                   # REST endpoints (12 controllers)
│   ├── master/                   # Master data CRUD (9 controllers)
│   │   ├── BranchMasterController.java
│   │   ├── GroupMasterController.java
│   │   ├── ItemMasterController.java
│   │   ├── LocationMasterController.java
│   │   ├── ManufacturerMasterController.java
│   │   ├── SupplierMasterController.java
│   │   ├── UnitMasterController.java
│   │   ├── RoleMasterController.java
│   │   └── ApprovalRuleMasterController.java
│   ├── GrnController.java        # Goods receipt
│   ├── IssueController.java      # Material issuance
│   ├── TransferController.java   # Inter-branch transfers
│   ├── StockController.java      # Stock queries
│   ├── InvoiceController.java    # Supplier invoices
│   └── ReportController.java     # 14 reports
│
├── dto/                          # Data Transfer Objects
│   ├── common/
│   │   └── ApiResponse.java      # Standard response wrapper
│   ├── inward/                   # GRN DTOs
│   ├── outward/                  # Issue DTOs
│   ├── transfer/                 # Transfer DTOs
│   ├── stock/                    # Stock DTOs
│   └── report/                   # Report DTOs (14+)
│
├── entity/                       # JPA Entities (25+ entities)
│   ├── master/                   # Master entities (11)
│   │   ├── BranchMaster.java
│   │   ├── GroupMaster.java
│   │   ├── SubGroupMaster.java
│   │   ├── ItemMaster.java
│   │   ├── LocationMaster.java
│   │   ├── ManufacturerMaster.java
│   │   ├── SupplierMaster.java
│   │   ├── UnitMaster.java
│   │   ├── RoleMaster.java
│   │   ├── UserRoleMap.java
│   │   └── ApprovalRuleMaster.java
│   ├── inward/                   # GRN entities
│   │   ├── GrnHeader.java
│   │   └── GrnDetail.java
│   ├── outward/                  # Issue entities
│   │   ├── IssueHeader.java
│   │   ├── IssueDetail.java
│   │   └── IssueFifoConsumption.java
│   ├── purchase/                 # PO and invoice entities
│   │   ├── PoHeader.java
│   │   ├── PoDetail.java
│   │   └── SupplierInvoice.java
│   ├── transfer/                 # Transfer entities
│   │   ├── StockTransferHeader.java
│   │   └── StockTransferDetail.java
│   └── stock/                    # Stock tracking
│       ├── MaterialStockLedger.java
│       └── BranchMaterialStock.java
│
├── repository/                   # Spring Data JPA (24 repos)
│   ├── master/                   # Master repositories (11)
│   ├── inward/                   # GRN repositories
│   ├── outward/                  # Issue repositories
│   ├── purchase/                 # PO repositories
│   ├── transfer/                 # Transfer repositories
│   ├── stock/                    # Stock repositories
│   └── report/                   # Report repository
│
├── service/                      # Business logic (18 services)
│   ├── master/                   # Master services (9)
│   ├── inward/
│   │   └── GrnService.java
│   ├── outward/
│   │   ├── IssueService.java
│   │   └── FifoService.java
│   ├── transfer/
│   │   └── TransferService.java
│   ├── purchase/
│   │   └── InvoiceService.java
│   ├── approval/
│   │   └── ApprovalService.java
│   └── stock/
│       ├── StockLedgerService.java
│       ├── AgingService.java
│       └── ReportService.java
│
└── exception/                    # Exception handling
    ├── MmsException.java
    ├── InsufficientStockException.java
    ├── ApprovalRequiredException.java
    └── GlobalExceptionHandler.java
```

---

## 🛠 Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.1
- **Language**: Java 17
- **Build Tool**: Maven
- **ORM**: Spring Data JPA / Hibernate
- **Database**: MySQL 8.0
- **Migration Tool**: Flyway (6 migration versions)
- **Code Generation**: Lombok

### Infrastructure
- **Cloud Storage**: AWS S3 (invoice PDFs)
- **Connection Pooling**: HikariCP
- **API Documentation**: RESTful design

### Development Tools
- **Java Version Manager**: SDKMAN (Java 11 on local, needs Java 17)
- **IDE**: IntelliJ IDEA / VS Code
- **Version Control**: Git

---

## 🚀 Getting Started

### Prerequisites
- Java 17+ (SDKMAN recommended)
- Maven 3.8+
- MySQL 8.0
- AWS credentials (for S3 invoice storage)

### Database Setup

1. **Create MySQL database:**
```sql
CREATE DATABASE mms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **Flyway will auto-create tables on startup** (6 migrations):
   - V1: Master tables (branch, item, supplier, etc.)
   - V2: Purchase tables (PO, supplier invoice)
   - V3: Inward tables (GRN)
   - V4: Outward tables (Issue, FIFO consumption)
   - V5: Transfer tables
   - V6: Stock ledger and summary tables

### Environment Variables

Set the following environment variables:

```bash
# Database
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=mms_db
export DB_USERNAME=root
export DB_PASSWORD=your_password

# AWS S3
export AWS_ACCESS_KEY=your_access_key
export AWS_SECRET_KEY=your_secret_key
```

### Build & Run

```bash
# Compile
mvn clean compile

# Run application
mvn spring-boot:run

# Or build JAR and run
mvn clean package
java -jar target/mms-0.0.1-SNAPSHOT.jar
```

### Access
- **Base URL**: `http://localhost:8080/mms`
- **API Base**: `http://localhost:8080/mms/api/v1`

---

## 📦 Core Modules

### 1. Master Data Management

Manages foundational data for the system:

| Module | Purpose | Key Entities |
|--------|---------|--------------|
| **Branch** | Warehouse/branch locations | BranchMaster |
| **Item Catalog** | Product/material master | ItemMaster, GroupMaster, SubGroupMaster |
| **Suppliers** | Vendor management | SupplierMaster, ManufacturerMaster |
| **Storage** | Location hierarchy (warehouse → shelf → bin) | LocationMaster |
| **Units** | Units of measurement (KG, LTR, PCS) | UnitMaster |
| **Roles** | User roles and permissions | RoleMaster, UserRoleMap |
| **Approval Rules** | Price variance thresholds | ApprovalRuleMaster |

### 2. Inward Module (GRN)

Handles goods receipt into warehouses:

- **GRN Creation**: Draft GRNs with line items
- **PO Matching**: Optional PO validation
- **Price Variance Detection**: Compares against last GRN rate
- **Approval Routing**: Auto-routes based on variance thresholds
- **Stock In**: Records MaterialStockLedger entry
- **FIFO Setup**: Initializes qty_remaining for batch tracking

**Status Flow**: DRAFT → PENDING_APPROVAL → POSTED

### 3. Outward Module (Issue)

Handles material consumption/issuance:

- **Issue Creation**: Draft issues with line items
- **Stock Validation**: Checks FIFO availability
- **FIFO Consumption**: Automatically consumes from oldest batches
- **Rate Calculation**: Weighted average from consumed GRN batches
- **Stock Out**: Records MaterialStockLedger entry
- **Batch Linkage**: Creates IssueFifoConsumption records

**Status Flow**: DRAFT → POSTED

### 4. Transfer Module

Manages inter-branch stock transfers:

- **3-Stage Workflow**: Created → In Transit → Received
- **Dispatch Phase**: Creates Issue at sender branch
- **Receipt Phase**: Creates GRN at receiver branch
- **Dual Ledger Entries**: TRANSFER_OUT (source) + TRANSFER_IN (destination)
- **Status Tracking**: Real-time transfer status

### 5. Stock Management

Provides real-time inventory visibility:

- **Stock Ledger**: Append-only transaction log (single source of truth)
- **Balance Queries**: Real-time stock by branch/item/location
- **Stock Summary**: Aggregated view (BranchMaterialStock)
- **Aging Analysis**: Age buckets for FIFO compliance
- **Weighted Average Costing**: Automatic cost calculation

### 6. Purchase Module

Manages procurement documents:

- **Purchase Orders**: PO creation and tracking
- **Supplier Invoices**: Invoice with PDF upload to S3
- **Three-Way Matching**: PO vs GRN vs Invoice comparison
- **Invoice Queries**: By supplier, date range

### 7. Approval Module

Enforces approval workflows:

- **Price Variance Detection**: New rate vs last GRN rate
- **Threshold Rules**: Configurable variance % limits
- **Role-Based Routing**: Routes to specific roles
- **Multi-Item Handling**: Aggregates variances across line items

### 8. Reporting Module

14 comprehensive reports for analytics and compliance.

---

## 🌐 API Documentation

### Base URL
```
http://localhost:8080/mms/api/v1
```

All responses wrapped in:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2026-02-12T10:30:00"
}
```

---

### 1. Master Data APIs

Base path: `/api/v1/master`

#### Branch Master
```http
GET    /master/branches              # List all branches
GET    /master/branches?name=Delhi   # Filter by name
GET    /master/branches/{branchId}   # Get by ID
POST   /master/branches              # Create branch
PUT    /master/branches/{branchId}   # Update branch
```

**Request Body (POST/PUT):**
```json
{
  "branchId": "BR001",
  "branchName": "Delhi Warehouse",
  "address": "Sector 18, Noida",
  "contactPerson": "Raj Kumar",
  "contactNumber": "9876543210",
  "active": true
}
```

#### Item Master
```http
GET    /master/items                              # List all items
GET    /master/items?name=Milk                    # Filter by name
GET    /master/items?groupId=GRP01&subGroupId=SG01 # Filter by group
GET    /master/items/{itemId}                      # Get by ID
POST   /master/items                               # Create item
PUT    /master/items/{itemId}                      # Update item
```

**Request Body (POST/PUT):**
```json
{
  "itemId": "ITM001",
  "itemName": "Full Cream Milk",
  "itemDesc": "Fresh full cream milk 1L pack",
  "groupId": "GRP01",
  "subGroupId": "SG01",
  "manufId": "MF001",
  "unitId": "LTR",
  "hsnCode": "0401",
  "gstPct": 5.0,
  "costPrice": 45.00,
  "mrp": 60.00,
  "reorderLevel": 100,
  "maxStockLevel": 1000,
  "active": true
}
```

#### Group & Sub-Group Master
```http
GET    /master/groups                              # List all groups
POST   /master/groups                              # Create group
GET    /master/groups/{groupId}/sub-groups         # List sub-groups
POST   /master/groups/{groupId}/sub-groups         # Create sub-group
```

#### Location Master (Storage Hierarchy)
```http
GET    /master/locations                           # List all locations
GET    /master/locations?branchId=BR001            # Filter by branch
GET    /master/locations?parentId=WH01             # Filter by parent
POST   /master/locations                           # Create location
```

**Request Body (Warehouse → Shelf → Bin):**
```json
{
  "locationId": "BIN001",
  "locationName": "Bin A1",
  "branchId": "BR001",
  "parentLocationId": "SHELF01",  // Optional hierarchy
  "locationType": "BIN",           // WAREHOUSE, SHELF, BIN
  "capacity": 500,
  "active": true
}
```

#### Supplier Master
```http
GET    /master/suppliers              # List all suppliers
POST   /master/suppliers              # Create supplier
PUT    /master/suppliers/{suppId}     # Update supplier
```

#### Manufacturer Master
```http
GET    /master/manufacturers          # List all manufacturers
POST   /master/manufacturers          # Create manufacturer
```

#### Unit Master
```http
GET    /master/units                  # List all units
POST   /master/units                  # Create unit (KG, LTR, PCS, etc.)
```

#### Role & Approval Rules
```http
GET    /master/roles                       # List all roles
POST   /master/roles                       # Create role
GET    /master/roles/user-roles?userId=U01 # Get user roles
POST   /master/roles/user-roles            # Map user to role

GET    /master/approval-rules              # List approval rules
POST   /master/approval-rules              # Create approval rule
```

**Approval Rule Example:**
```json
{
  "ruleId": "APR001",
  "txnType": "GRN",                    // GRN, ISSUE, TRANSFER
  "conditionType": "PRICE_VARIANCE",   // Condition type
  "minValue": 5.0,                     // Min variance % threshold
  "maxValue": 10.0,                    // Max variance % threshold
  "requiredRole": "PURCHASE_MANAGER",
  "active": true
}
```

---

### 2. GRN (Goods Receipt) APIs

Base path: `/api/v1/grn`

#### Create GRN (Draft)
```http
POST /api/v1/grn
```

**Request:**
```json
{
  "branchId": "BR001",
  "suppId": "SUP001",
  "poId": "PO12345",                  // Optional
  "invoiceId": "INV001",              // Optional
  "grnDate": "2026-02-12",
  "remarks": "Regular delivery",
  "details": [
    {
      "itemId": "ITM001",
      "unitId": "LTR",
      "locationId": "BIN001",
      "qtyReceived": 500,
      "rate": 45.50,
      "remarks": "Good quality"
    },
    {
      "itemId": "ITM002",
      "unitId": "KG",
      "locationId": "BIN002",
      "qtyReceived": 200,
      "rate": 120.00
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "GRN created successfully",
  "data": {
    "grnId": 1001,
    "grnDate": "2026-02-12",
    "status": "DRAFT",
    "branchId": "BR001",
    "suppId": "SUP001",
    "totalQty": 700,
    "totalAmount": 46750.00,
    "details": [ ... ]
  }
}
```

#### Get Price Suggestion
```http
GET /api/v1/grn/price-suggestion?branchId=BR001&itemId=ITM001
```

**Response:**
```json
{
  "success": true,
  "data": {
    "itemId": "ITM001",
    "lastGrnRate": 45.00,
    "lastGrnDate": "2026-02-10",
    "itemMasterCost": 44.50,
    "suggestedRate": 45.00
  }
}
```

#### Submit GRN for Approval
```http
POST /api/v1/grn/{grnId}/submit
```

**Response (if auto-approved):**
```json
{
  "success": true,
  "message": "GRN auto-posted (no approval required)",
  "data": {
    "grnId": 1001,
    "status": "POSTED",
    "requiresApproval": false
  }
}
```

**Response (if approval required):**
```json
{
  "success": false,
  "message": "Approval required due to price variance",
  "data": {
    "grnId": 1001,
    "status": "PENDING_APPROVAL",
    "requiresApproval": true,
    "variances": [
      {
        "itemId": "ITM001",
        "newRate": 50.00,
        "lastGrnRate": 45.00,
        "varianceAmount": 5.00,
        "variancePct": 11.11,
        "requiredRole": "PURCHASE_MANAGER"
      }
    ]
  }
}
```

#### Approve GRN
```http
POST /api/v1/grn/{grnId}/approve
```

**Response:**
```json
{
  "success": true,
  "message": "GRN approved and posted successfully",
  "data": {
    "grnId": 1001,
    "status": "POSTED",
    "ledgerEntriesCreated": 2,
    "stockUpdated": true
  }
}
```

#### Get GRN Details
```http
GET /api/v1/grn/{grnId}
GET /api/v1/grn/branch/{branchId}
GET /api/v1/grn/pending-approval
```

---

### 3. Issue (Material Outward) APIs

Base path: `/api/v1/issue`

#### Check Stock Availability
```http
GET /api/v1/issue/check-stock?branchId=BR001&itemId=ITM001&locationId=BIN001&qty=100
```

**Response:**
```json
{
  "success": true,
  "data": {
    "available": true,
    "requestedQty": 100,
    "availableQty": 450,
    "fifoAvailable": true
  }
}
```

#### Create Issue (Draft)
```http
POST /api/v1/issue
```

**Request:**
```json
{
  "branchId": "BR001",
  "issueType": "PRODUCTION",          // PRODUCTION, SALE, WASTAGE
  "issueDate": "2026-02-12",
  "remarks": "Production batch #45",
  "details": [
    {
      "itemId": "ITM001",
      "locationId": "BIN001",
      "qtyIssued": 100
      // Note: rate is NOT provided, it's calculated from FIFO
    }
  ]
}
```

#### Post Issue (FIFO Consumption)
```http
POST /api/v1/issue/{issueId}/post
```

**Response:**
```json
{
  "success": true,
  "message": "Issue posted successfully with FIFO consumption",
  "data": {
    "issueId": 2001,
    "status": "POSTED",
    "issueDate": "2026-02-12",
    "details": [
      {
        "itemId": "ITM001",
        "qtyIssued": 100,
        "rate": 45.20,              // Weighted avg from consumed GRNs
        "amount": 4520.00,
        "fifoConsumptions": [
          {
            "grnId": 998,
            "grnDate": "2026-02-01",
            "qtyConsumed": 60,
            "rate": 45.00
          },
          {
            "grnId": 1001,
            "grnDate": "2026-02-10",
            "qtyConsumed": 40,
            "rate": 45.50
          }
        ]
      }
    ]
  }
}
```

#### Get Issue Details
```http
GET /api/v1/issue/{issueId}
GET /api/v1/issue/branch/{branchId}
```

---

### 4. Stock Transfer APIs

Base path: `/api/v1/transfer`

#### Create Transfer
```http
POST /api/v1/transfer
```

**Request:**
```json
{
  "fromBranchId": "BR001",
  "toBranchId": "BR002",
  "transferDate": "2026-02-12",
  "remarks": "Stock rebalancing",
  "details": [
    {
      "itemId": "ITM001",
      "qtyToTransfer": 200,
      "fromLocationId": "BIN001",
      "toLocationId": "BIN101"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "data": {
    "transferId": 3001,
    "status": "CREATED",
    "fromBranchId": "BR001",
    "toBranchId": "BR002",
    "transferDate": "2026-02-12",
    "details": [ ... ]
  }
}
```

#### Dispatch Transfer (Sender Issues Stock)
```http
POST /api/v1/transfer/{transferId}/dispatch
```

**Response:**
```json
{
  "success": true,
  "message": "Transfer dispatched successfully",
  "data": {
    "transferId": 3001,
    "status": "IN_TRANSIT",
    "senderIssueId": 2002,
    "dispatchDate": "2026-02-12"
  }
}
```

#### Receive Transfer (Receiver Records GRN)
```http
POST /api/v1/transfer/{transferId}/receive
```

**Request:**
```json
{
  "receivedDate": "2026-02-13",
  "details": [
    {
      "itemId": "ITM001",
      "qtyReceived": 200,          // May differ from qty sent
      "locationId": "BIN101"
    }
  ]
}
```

**Response:**
```json
{
  "success": true,
  "message": "Transfer received successfully",
  "data": {
    "transferId": 3001,
    "status": "RECEIVED",
    "receiverGrnId": 1002,
    "receivedDate": "2026-02-13"
  }
}
```

#### Get Transfer Details
```http
GET /api/v1/transfer/{transferId}
GET /api/v1/transfer/pending-dispatch/{branchId}
GET /api/v1/transfer/pending-receipt/{branchId}
```

---

### 5. Stock Query APIs

Base path: `/api/v1/stock`

#### Get Stock Summary
```http
GET /api/v1/stock/summary/{branchId}
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "branchId": "BR001",
      "itemId": "ITM001",
      "itemName": "Full Cream Milk",
      "locationId": "BIN001",
      "qtyOnHand": 450,
      "avgCost": 45.30,
      "totalValue": 20385.00,
      "unitId": "LTR",
      "lastUpdated": "2026-02-12T10:30:00"
    },
    ...
  ]
}
```

#### Get Specific Balance
```http
GET /api/v1/stock/balance?branchId=BR001&itemId=ITM001&locationId=BIN001
```

**Response:**
```json
{
  "success": true,
  "data": {
    "branchId": "BR001",
    "itemId": "ITM001",
    "locationId": "BIN001",
    "qtyOnHand": 450,
    "avgCost": 45.30,
    "unitId": "LTR"
  }
}
```

#### Get Aging Report
```http
GET /api/v1/stock/aging/{branchId}?asOfDate=2026-02-12
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "branchId": "BR001",
      "itemId": "ITM001",
      "locationId": "BIN001",
      "agingBucket": "0-30 days",
      "qty": 200,
      "value": 9100.00,
      "oldestDate": "2026-02-10"
    },
    {
      "branchId": "BR001",
      "itemId": "ITM001",
      "locationId": "BIN001",
      "agingBucket": "31-60 days",
      "qty": 150,
      "value": 6750.00,
      "oldestDate": "2026-01-15"
    },
    {
      "branchId": "BR001",
      "itemId": "ITM001",
      "locationId": "BIN001",
      "agingBucket": "90+ days",
      "qty": 100,
      "value": 4500.00,
      "oldestDate": "2025-11-10"
    }
  ]
}
```

---

### 6. Invoice APIs

Base path: `/api/v1/invoice`

#### Create Invoice with PDF
```http
POST /api/v1/invoice
Content-Type: multipart/form-data
```

**Form Data:**
```
invoiceNumber: INV-2026-001
suppId: SUP001
invoiceDate: 2026-02-12
invoiceAmount: 50000.00
taxAmount: 2500.00
totalAmount: 52500.00
dueDate: 2026-03-12
file: [PDF file upload]
```

**Response:**
```json
{
  "success": true,
  "data": {
    "invoiceId": "INV001",
    "invoiceNumber": "INV-2026-001",
    "suppId": "SUP001",
    "invoiceDate": "2026-02-12",
    "totalAmount": 52500.00,
    "pdfUrl": "https://mms-invoices.s3.ap-south-1.amazonaws.com/INV-2026-001.pdf",
    "uploadedAt": "2026-02-12T10:30:00"
  }
}
```

#### Upload/Update Invoice PDF
```http
POST /api/v1/invoice/{invoiceId}/upload
Content-Type: multipart/form-data
```

#### Get Invoice
```http
GET /api/v1/invoice/{invoiceId}
GET /api/v1/invoice/supplier/{suppId}
GET /api/v1/invoice/by-date?startDate=2026-02-01&endDate=2026-02-28
```

---

### 7. Reporting APIs

Base path: `/api/v1/reports`

All reports accept **ReportFilterDTO** as query parameters:

**Common Query Parameters:**
- `branchIds`: Comma-separated list (e.g., `BR001,BR002`) or empty for all branches
- `fromDate`: Start date (YYYY-MM-DD)
- `toDate`: End date (YYYY-MM-DD)
- `itemId`: Filter by item
- `suppId`: Filter by supplier
- `status`: Filter by status
- `minVariancePct`: Minimum variance percentage
- `agingDays`: Aging threshold

#### 1. Current Stock Report
```http
GET /api/v1/reports/current-stock?branchIds=BR001,BR002&itemId=ITM001
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "branchId": "BR001",
      "branchName": "Delhi Warehouse",
      "itemId": "ITM001",
      "itemName": "Full Cream Milk",
      "locationId": "BIN001",
      "qtyOnHand": 450,
      "avgCost": 45.30,
      "totalValue": 20385.00,
      "unitId": "LTR"
    },
    ...
  ]
}
```

#### 2. Consolidated Stock Summary
```http
GET /api/v1/reports/consolidated-stock?branchIds=BR001,BR002
```

#### 3. Stock Ledger Report (Audit Trail)
```http
GET /api/v1/reports/stock-ledger?branchIds=BR001&itemId=ITM001&fromDate=2026-02-01&toDate=2026-02-12
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "ledgerId": 10001,
      "branchId": "BR001",
      "itemId": "ITM001",
      "locationId": "BIN001",
      "txnDate": "2026-02-12",
      "txnType": "GRN",
      "refId": "1001",
      "qtyIn": 500,
      "qtyOut": 0,
      "rate": 45.50,
      "balanceQty": 950,
      "remarks": "Supplier delivery"
    },
    {
      "ledgerId": 10002,
      "branchId": "BR001",
      "itemId": "ITM001",
      "locationId": "BIN001",
      "txnDate": "2026-02-12",
      "txnType": "ISSUE",
      "refId": "2001",
      "qtyIn": 0,
      "qtyOut": 100,
      "rate": 45.20,
      "balanceQty": 850,
      "remarks": "Production batch"
    },
    ...
  ]
}
```

#### 4. Stock Aging Report
```http
GET /api/v1/reports/stock-aging?branchIds=BR001&asOfDate=2026-02-12
```

#### 5. FIFO Consumption Report
```http
GET /api/v1/reports/fifo-consumption?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "issueId": 2001,
      "issueDate": "2026-02-12",
      "branchId": "BR001",
      "itemId": "ITM001",
      "qtyIssued": 100,
      "consumptions": [
        {
          "grnId": 998,
          "grnDate": "2026-02-01",
          "qtyConsumed": 60,
          "rate": 45.00,
          "amount": 2700.00
        },
        {
          "grnId": 1001,
          "grnDate": "2026-02-10",
          "qtyConsumed": 40,
          "rate": 45.50,
          "amount": 1820.00
        }
      ],
      "totalAmount": 4520.00,
      "weightedAvgRate": 45.20
    },
    ...
  ]
}
```

#### 6. GRN Summary Report
```http
GET /api/v1/reports/grn-summary?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

#### 7. GRN vs Invoice Comparison (3-Way Match)
```http
GET /api/v1/reports/grn-invoice-comparison?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

#### 8. Price Variance Report
```http
GET /api/v1/reports/price-variance?branchIds=BR001&minVariancePct=5.0&fromDate=2026-02-01&toDate=2026-02-12
```

#### 9. PO vs GRN Report
```http
GET /api/v1/reports/po-grn-comparison?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

#### 10. Inter-Branch Transfer Report
```http
GET /api/v1/reports/inter-branch-transfers?branchIds=BR001,BR002&fromDate=2026-02-01&toDate=2026-02-12
```

#### 11. Issue to Production Report
```http
GET /api/v1/reports/issue-production?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

#### 12. Non-Moving Stock Report
```http
GET /api/v1/reports/non-moving-stock?branchIds=BR001&agingDays=90
```

#### 13. Supplier Performance Report
```http
GET /api/v1/reports/supplier-performance?suppId=SUP001&fromDate=2026-01-01&toDate=2026-02-12
```

#### 14. Audit & Exception Report
```http
GET /api/v1/reports/audit-exceptions?branchIds=BR001&fromDate=2026-02-01&toDate=2026-02-12
```

---

## 💾 Database Schema

### Master Tables (V1)

#### branch_master
```sql
CREATE TABLE branch_master (
  branch_id VARCHAR(20) PRIMARY KEY,
  branch_name VARCHAR(100) NOT NULL,
  address VARCHAR(255),
  contact_person VARCHAR(100),
  contact_number VARCHAR(20),
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### item_master
```sql
CREATE TABLE item_master (
  item_id VARCHAR(20) PRIMARY KEY,
  item_name VARCHAR(100) NOT NULL,
  item_desc VARCHAR(255),
  group_id VARCHAR(20),
  sub_group_id VARCHAR(20),
  manuf_id VARCHAR(20),
  unit_id VARCHAR(20),
  hsn_code VARCHAR(20),
  gst_pct DECIMAL(5,2),
  cost_price DECIMAL(10,2),
  mrp DECIMAL(10,2),
  reorder_level INT,
  max_stock_level INT,
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (group_id, sub_group_id) REFERENCES sub_group_master(group_id, sub_group_id),
  FOREIGN KEY (manuf_id) REFERENCES manufacturer_master(manuf_id),
  FOREIGN KEY (unit_id) REFERENCES unit_master(unit_id)
);
```

#### location_master (Hierarchical)
```sql
CREATE TABLE location_master (
  location_id VARCHAR(20) PRIMARY KEY,
  location_name VARCHAR(100) NOT NULL,
  branch_id VARCHAR(20),
  parent_location_id VARCHAR(20),           -- Self-reference for hierarchy
  location_type VARCHAR(20),                -- WAREHOUSE, SHELF, BIN
  capacity INT,
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
  FOREIGN KEY (parent_location_id) REFERENCES location_master(location_id)
);
```

#### approval_rule_master
```sql
CREATE TABLE approval_rule_master (
  rule_id VARCHAR(20) PRIMARY KEY,
  txn_type VARCHAR(20),                     -- GRN, ISSUE, TRANSFER
  condition_type VARCHAR(50),               -- PRICE_VARIANCE, QTY_VARIANCE
  min_value DECIMAL(10,2),
  max_value DECIMAL(10,2),
  required_role VARCHAR(20),
  active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (required_role) REFERENCES role_master(role_id)
);
```

### Transactional Tables

#### grn_header (V3)
```sql
CREATE TABLE grn_header (
  grn_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  branch_id VARCHAR(20),
  supp_id VARCHAR(20),
  po_id VARCHAR(20),
  invoice_id VARCHAR(20),
  grn_date DATE,
  grn_no VARCHAR(50),
  status VARCHAR(20),                       -- DRAFT, PENDING_APPROVAL, POSTED
  remarks VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
  FOREIGN KEY (supp_id) REFERENCES supplier_master(supp_id)
);
```

#### grn_detail (V3)
```sql
CREATE TABLE grn_detail (
  grn_id BIGINT,
  item_id VARCHAR(20),
  unit_id VARCHAR(20),
  location_id VARCHAR(20),
  qty_received DECIMAL(10,2),
  rate DECIMAL(10,2),
  qty_remaining DECIMAL(10,2),              -- For FIFO tracking
  remarks VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  PRIMARY KEY (grn_id, item_id),
  FOREIGN KEY (grn_id) REFERENCES grn_header(grn_id),
  FOREIGN KEY (item_id) REFERENCES item_master(item_id)
);
```

#### issue_header (V4)
```sql
CREATE TABLE issue_header (
  issue_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  branch_id VARCHAR(20),
  issue_type VARCHAR(20),                   -- PRODUCTION, SALE, WASTAGE
  issue_date DATE,
  issue_no VARCHAR(50),
  status VARCHAR(20),                       -- DRAFT, POSTED
  remarks VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id)
);
```

#### issue_detail (V4)
```sql
CREATE TABLE issue_detail (
  issue_id BIGINT,
  item_id VARCHAR(20),
  location_id VARCHAR(20),
  qty_issued DECIMAL(10,2),
  rate DECIMAL(10,2),                       -- Calculated from FIFO, not user input
  amount DECIMAL(12,2),
  remarks VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  PRIMARY KEY (issue_id, item_id),
  FOREIGN KEY (issue_id) REFERENCES issue_header(issue_id),
  FOREIGN KEY (item_id) REFERENCES item_master(item_id)
);
```

#### issue_fifo_consumption (V4)
```sql
CREATE TABLE issue_fifo_consumption (
  consumption_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  issue_id BIGINT,
  item_id VARCHAR(20),
  grn_id BIGINT,                            -- Which GRN batch was consumed
  qty_consumed DECIMAL(10,2),
  rate DECIMAL(10,2),
  amount DECIMAL(12,2),
  created_at TIMESTAMP,
  FOREIGN KEY (issue_id, item_id) REFERENCES issue_detail(issue_id, item_id),
  FOREIGN KEY (grn_id, item_id) REFERENCES grn_detail(grn_id, item_id)
);
```

#### stock_transfer_header (V5)
```sql
CREATE TABLE stock_transfer_header (
  transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  from_branch_id VARCHAR(20),
  to_branch_id VARCHAR(20),
  transfer_date DATE,
  status VARCHAR(20),                       -- CREATED, IN_TRANSIT, RECEIVED
  sender_issue_id BIGINT,                   -- Issue created during dispatch
  receiver_grn_id BIGINT,                   -- GRN created during receipt
  remarks VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  FOREIGN KEY (from_branch_id) REFERENCES branch_master(branch_id),
  FOREIGN KEY (to_branch_id) REFERENCES branch_master(branch_id)
);
```

### Stock Tables (V6)

#### material_stock_ledger (Append-Only)
```sql
CREATE TABLE material_stock_ledger (
  ledger_id BIGINT AUTO_INCREMENT PRIMARY KEY,
  branch_id VARCHAR(20),
  item_id VARCHAR(20),
  location_id VARCHAR(20),
  txn_date TIMESTAMP,
  txn_type VARCHAR(20),                     -- GRN, ISSUE, TRANSFER_IN, TRANSFER_OUT
  ref_id VARCHAR(50),                       -- Reference to GRN/Issue/Transfer
  qty_in DECIMAL(10,2),
  qty_out DECIMAL(10,2),
  rate DECIMAL(10,2),
  balance_qty DECIMAL(10,2),
  remarks VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  -- NO updated_at column - append-only table
  FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
  FOREIGN KEY (item_id) REFERENCES item_master(item_id),
  INDEX idx_branch_item_loc (branch_id, item_id, location_id),
  INDEX idx_txn_date (txn_date)
);
```

#### branch_material_stock (Summary Table)
```sql
CREATE TABLE branch_material_stock (
  branch_id VARCHAR(20),
  item_id VARCHAR(20),
  location_id VARCHAR(20),
  qty_on_hand DECIMAL(10,2),
  avg_cost DECIMAL(10,2),
  last_updated TIMESTAMP,
  PRIMARY KEY (branch_id, item_id, location_id),
  FOREIGN KEY (branch_id) REFERENCES branch_master(branch_id),
  FOREIGN KEY (item_id) REFERENCES item_master(item_id)
);
```

### Entity Relationships Diagram

```
branch_master (1) ----< (*) location_master
                 |
                 +----< (*) item_master
                 |
                 +----< (*) grn_header
                 |
                 +----< (*) issue_header
                 |
                 +----< (*) material_stock_ledger

item_master (*) ----< (1) sub_group_master
           |
           +----< (1) manufacturer_master
           |
           +----< (1) unit_master
           |
           +----< (*) grn_detail
           |
           +----< (*) issue_detail

grn_header (1) ----< (*) grn_detail
          |
          +----< (1) supplier_master
          |
          +----< (1) po_header (optional)

grn_detail (1) ----< (*) issue_fifo_consumption

issue_header (1) ----< (*) issue_detail

issue_detail (1) ----< (*) issue_fifo_consumption

stock_transfer_header (*) ----< (1) issue_header (sender)
                         |
                         +----< (1) grn_header (receiver)
```

---

## 🧠 Business Logic Patterns

### 1. Append-Only Ledger Pattern

**Concept**: `material_stock_ledger` is the **single source of truth** for all inventory movements.

**Rules:**
- ✅ INSERT allowed (every transaction creates new ledger entry)
- ❌ UPDATE forbidden (no modifications to history)
- ❌ DELETE forbidden (no deletion of audit trail)

**Benefits:**
- Complete audit trail
- Point-in-time stock queries
- Prevents accidental corrections
- Regulatory compliance

**Example:**
```java
// StockLedgerService.java
public void recordStockIn(String branchId, String itemId,
                          BigDecimal qty, BigDecimal rate) {
    MaterialStockLedger ledger = MaterialStockLedger.builder()
        .branchId(branchId)
        .itemId(itemId)
        .txnType("GRN")
        .qtyIn(qty)
        .qtyOut(BigDecimal.ZERO)
        .rate(rate)
        .build();

    ledgerRepository.save(ledger);  // INSERT only, never UPDATE
}
```

---

### 2. FIFO with Batch Tracking

**Concept**: Issues consume from oldest GRN batches first, with full traceability.

**Implementation:**
1. **GRN Detail**: Tracks `qty_remaining` for each receipt
2. **FIFO Service**: Selects oldest batches (ORDER BY grn_date ASC)
3. **Issue FIFO Consumption**: Links each issue to consumed GRN batches
4. **Rate Calculation**: Weighted average of consumed batch rates

**Example Flow:**
```
GRN 1: 100 units @ ₹45/unit (2026-02-01)
GRN 2: 200 units @ ₹50/unit (2026-02-10)

Issue: 150 units

FIFO Consumption:
- Consume 100 from GRN 1 @ ₹45 = ₹4,500
- Consume 50 from GRN 2 @ ₹50 = ₹2,500
- Total: ₹7,000
- Weighted Avg Rate: ₹7,000 / 150 = ₹46.67
```

**Code:**
```java
// FifoService.java
public BigDecimal consumeStock(String branchId, String itemId, BigDecimal qtyNeeded) {
    List<GrnDetail> availableBatches = grnDetailRepository
        .findAvailableStockForFifo(branchId, itemId);  // ORDER BY grn_date ASC

    BigDecimal totalAmount = BigDecimal.ZERO;
    BigDecimal remaining = qtyNeeded;

    for (GrnDetail batch : availableBatches) {
        if (remaining.compareTo(BigDecimal.ZERO) == 0) break;

        BigDecimal toConsume = remaining.min(batch.getQtyRemaining());

        // Create consumption record
        IssueFifoConsumption consumption = new IssueFifoConsumption();
        consumption.setGrnId(batch.getGrnId());
        consumption.setQtyConsumed(toConsume);
        consumption.setRate(batch.getRate());
        consumptionRepository.save(consumption);

        // Update batch remaining
        batch.setQtyRemaining(batch.getQtyRemaining().subtract(toConsume));
        grnDetailRepository.save(batch);

        totalAmount = totalAmount.add(toConsume.multiply(batch.getRate()));
        remaining = remaining.subtract(toConsume);
    }

    // Return weighted average rate
    return totalAmount.divide(qtyNeeded, 2, RoundingMode.HALF_UP);
}
```

---

### 3. Price Variance & Approval Workflow

**Concept**: Automatically route GRNs for approval if price variance exceeds thresholds.

**Price Reference Source:**
1. **Primary**: Last GRN rate from ledger (same branch + item)
2. **Fallback**: Item master cost price

**Variance Calculation:**
```
Variance % = ((New Rate - Last GRN Rate) / Last GRN Rate) * 100
```

**Approval Logic:**
```java
// ApprovalService.java
public ApprovalDecision checkGrnApproval(GrnHeader grn) {
    List<PriceVarianceInfo> variances = new ArrayList<>();

    for (GrnDetail detail : grn.getDetails()) {
        BigDecimal lastRate = getLastGrnRate(grn.getBranchId(), detail.getItemId());
        BigDecimal newRate = detail.getRate();

        BigDecimal variancePct = newRate.subtract(lastRate)
                                        .divide(lastRate, 4, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100));

        // Check against approval rules
        ApprovalRuleMaster rule = ruleRepository.findByTxnTypeAndVariance(
            "GRN", variancePct);

        if (rule != null) {
            variances.add(new PriceVarianceInfo(
                detail.getItemId(),
                lastRate,
                newRate,
                variancePct,
                rule.getRequiredRole()
            ));
        }
    }

    if (!variances.isEmpty()) {
        return ApprovalDecision.requiresApproval(variances);
    }

    return ApprovalDecision.autoApprove();
}
```

**Example Rules:**
```
Rule 1: GRN, PRICE_VARIANCE, 0-5%    → Auto-approve
Rule 2: GRN, PRICE_VARIANCE, 5-10%   → Requires PURCHASE_MANAGER
Rule 3: GRN, PRICE_VARIANCE, 10%+    → Requires FINANCE_HEAD
```

---

### 4. Inter-Branch Transfer as Dual Transactions

**Concept**: Transfer = Issue (source) + GRN (destination)

**3-Stage Workflow:**

**Stage 1: CREATE**
```
- User creates transfer request
- Status: CREATED
- No stock impact yet
```

**Stage 2: DISPATCH**
```
- System creates Issue at sender branch
- Records TRANSFER_OUT ledger entry
- Updates sender_issue_id
- Status: IN_TRANSIT
```

**Stage 3: RECEIVE**
```
- System creates GRN at receiver branch
- Records TRANSFER_IN ledger entry
- Updates receiver_grn_id
- Status: RECEIVED
```

**Code:**
```java
// TransferService.java
@Transactional
public void dispatchTransfer(Long transferId) {
    StockTransferHeader transfer = transferRepository.findById(transferId)
        .orElseThrow();

    // Create Issue at sender
    IssueHeader issue = new IssueHeader();
    issue.setBranchId(transfer.getFromBranchId());
    issue.setIssueType("TRANSFER_OUT");
    issue.setIssueDate(LocalDate.now());

    for (StockTransferDetail detail : transfer.getDetails()) {
        IssueDetail issueDetail = new IssueDetail();
        issueDetail.setItemId(detail.getItemId());
        issueDetail.setQtyIssued(detail.getQtySent());
        issue.getDetails().add(issueDetail);
    }

    issueService.postIssue(issue.getIssueId());  // FIFO consumption

    // Update transfer
    transfer.setSenderIssueId(issue.getIssueId());
    transfer.setStatus("IN_TRANSIT");
    transferRepository.save(transfer);
}

@Transactional
public void receiveTransfer(Long transferId, TransferReceiptRequest request) {
    StockTransferHeader transfer = transferRepository.findById(transferId)
        .orElseThrow();

    // Create GRN at receiver
    GrnHeader grn = new GrnHeader();
    grn.setBranchId(transfer.getToBranchId());
    grn.setGrnDate(request.getReceivedDate());

    for (TransferReceiptDetail detail : request.getDetails()) {
        GrnDetail grnDetail = new GrnDetail();
        grnDetail.setItemId(detail.getItemId());
        grnDetail.setQtyReceived(detail.getQtyReceived());
        // Rate comes from sender issue
        grn.getDetails().add(grnDetail);
    }

    grnService.submitGrn(grn.getGrnId());  // May require approval

    // Update transfer
    transfer.setReceiverGrnId(grn.getGrnId());
    transfer.setStatus("RECEIVED");
    transferRepository.save(transfer);
}
```

---

### 5. Calculated vs Stored Fields

**Calculated Fields (Not Stored):**
- Stock aging buckets (calculated from ledger `txn_date`)
- FIFO consumption linkage (queried from `issue_fifo_consumption`)
- Balance at specific point in time (sum ledger entries up to date)

**Stored Fields (Updated on Transaction):**
- `qty_on_hand` in `branch_material_stock` (updated on GRN/Issue post)
- `avg_cost` in `branch_material_stock` (weighted average updated on each GRN)
- `qty_remaining` in `grn_detail` (decremented on FIFO consumption)

**Why This Hybrid Approach?**
- Performance: Avoid expensive aggregations for frequent queries
- Accuracy: Ledger remains immutable source of truth
- Flexibility: Can recalculate summaries if needed

---

### 6. Lazy-Loaded Relationships Pattern

**Problem**: N+1 query issues with eager loading

**Solution**: All relationships use `FetchType.LAZY` with read-only join columns

**Example:**
```java
@Entity
public class GrnDetail {
    @Id
    private Long grnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grn_id", insertable = false, updatable = false)
    private GrnHeader grnHeader;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", insertable = false, updatable = false)
    private ItemMaster itemMaster;
}
```

**Benefits:**
- Prevents accidental N+1 queries
- Forces explicit fetch joins when needed
- Reduces memory footprint

---

### 7. Composite Primary Key Pattern

**Used For:**
- Detail tables (GrnDetail, IssueDetail, PoDetail)
- Mapping tables (UserRoleMap)
- Hierarchical tables (SubGroupMaster)

**Example:**
```java
@Embeddable
public class GrnDetailId implements Serializable {
    private Long grnId;
    private String itemId;
}

@Entity
public class GrnDetail {
    @EmbeddedId
    private GrnDetailId id;

    // Other fields...
}
```

**Benefits:**
- Ensures uniqueness within parent context
- Prevents duplicate line items
- Natural database design

---

### 8. User-Provided Master IDs

**All master entity IDs are Strings, not auto-generated:**

```java
// ✅ Correct
BranchMaster branch = BranchMaster.builder()
    .branchId("BR001")  // User specifies
    .branchName("Delhi")
    .build();

// ❌ Wrong
// IDs are NOT auto-generated for master tables
```

**Transactional IDs are auto-generated:**
```java
// ✅ Correct
GrnHeader grn = GrnHeader.builder()
    .branchId("BR001")
    // grnId is auto-generated by database
    .build();
```

---

### 9. Transaction Scoping Pattern

**All write operations use `@Transactional`:**

```java
@Service
public class GrnService {

    @Transactional
    public GrnResponse createGrn(GrnCreateRequest request) {
        // Multiple operations in single transaction
        GrnHeader header = createHeader(request);
        createDetails(header, request.getDetails());
        return toResponse(header);
    }

    @Transactional
    public void submitGrn(Long grnId) {
        GrnHeader grn = grnRepository.findById(grnId).orElseThrow();

        ApprovalDecision decision = approvalService.checkGrnApproval(grn);

        if (decision.isApprovalRequired()) {
            grn.setStatus("PENDING_APPROVAL");
            throw new ApprovalRequiredException(decision.getVariances());
        }

        // Auto-approve and post
        postGrn(grnId);
    }

    @Transactional
    public void postGrn(Long grnId) {
        GrnHeader grn = grnRepository.findById(grnId).orElseThrow();

        // Multi-step atomic operation
        for (GrnDetail detail : grn.getDetails()) {
            ledgerService.recordStockIn(...);
            stockService.updateBranchStock(...);
        }

        grn.setStatus("POSTED");
        grnRepository.save(grn);
    }
}
```

---

### 10. Standard Response Wrapper Pattern

**All REST responses wrapped in `ApiResponse<T>`:**

```java
@RestController
@RequestMapping("/api/v1/grn")
public class GrnController {

    @PostMapping
    public ApiResponse<GrnResponse> createGrn(@RequestBody GrnCreateRequest request) {
        GrnResponse grn = grnService.createGrn(request);
        return ApiResponse.success("GRN created successfully", grn);
    }

    @GetMapping("/{grnId}")
    public ApiResponse<GrnResponse> getGrn(@PathVariable Long grnId) {
        GrnResponse grn = grnService.getGrn(grnId);
        return ApiResponse.success("GRN fetched successfully", grn);
    }
}
```

**ApiResponse Structure:**
```java
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
}
```

---

## 🔄 Key Workflows

### Workflow 1: GRN Processing

```
┌─────────────┐
│ Create GRN  │
│  (DRAFT)    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ Validate PO (if any)│
└──────┬──────────────┘
       │
       ▼
┌─────────────────────┐
│   Submit GRN        │
└──────┬──────────────┘
       │
       ▼
┌──────────────────────────┐
│ Check Price Variance     │
│ (vs Last GRN Rate)       │
└───────┬──────────────────┘
        │
        ├─── Variance > Threshold ──┐
        │                            │
        ▼                            ▼
┌──────────────┐         ┌──────────────────┐
│ Auto-Approve │         │ PENDING_APPROVAL │
└──────┬───────┘         └─────────┬────────┘
       │                           │
       │                           ▼
       │                  ┌─────────────────┐
       │                  │ User Approves   │
       │                  └─────────┬───────┘
       │                            │
       └────────────┬───────────────┘
                    │
                    ▼
           ┌────────────────┐
           │   POST GRN     │
           └────────┬───────┘
                    │
                    ├──► Create Ledger Entry (TXN_TYPE=GRN)
                    │
                    └──► Update BranchMaterialStock
                         (qty_on_hand, avg_cost)
```

---

### Workflow 2: Issue Processing with FIFO

```
┌─────────────┐
│ Create Issue│
│  (DRAFT)    │
└──────┬──────┘
       │
       ▼
┌─────────────────────┐
│ Check Stock         │
│ Availability (FIFO) │
└──────┬──────────────┘
       │
       ├─── Insufficient ──► Throw InsufficientStockException
       │
       ▼
┌─────────────┐
│ Post Issue  │
└──────┬──────┘
       │
       ▼
┌───────────────────────────┐
│ For Each Line Item:       │
│                            │
│ 1. Get Available GRN      │
│    Batches (oldest first) │
│                            │
│ 2. Consume from Batches   │
│    ├─► Create             │
│    │   IssueFifoConsumption│
│    └─► Update             │
│        GrnDetail.qtyRemaining│
│                            │
│ 3. Calculate Weighted Avg │
│    Rate                    │
│                            │
│ 4. Create Ledger Entry    │
│    (TXN_TYPE=ISSUE)        │
│                            │
│ 5. Update BranchMaterial  │
│    Stock (qty_on_hand)     │
└───────────────────────────┘
```

**FIFO Consumption Example:**
```
Available Batches:
GRN 1: 100 units @ ₹45 (2026-02-01)
GRN 2: 200 units @ ₹50 (2026-02-10)
GRN 3: 150 units @ ₹48 (2026-02-12)

Issue Requirement: 250 units

Consumption:
┌─────────────────────────────────┐
│ Consume 100 from GRN 1 @ ₹45    │ ← Oldest
│ GrnDetail.qtyRemaining: 100→0   │
├─────────────────────────────────┤
│ Consume 150 from GRN 2 @ ₹50    │ ← Next oldest
│ GrnDetail.qtyRemaining: 200→50  │
└─────────────────────────────────┘

Weighted Avg Rate:
= (100×₹45 + 150×₹50) / 250
= (₹4,500 + ₹7,500) / 250
= ₹48/unit

IssueDetail.rate = ₹48
IssueDetail.amount = 250 × ₹48 = ₹12,000
```

---

### Workflow 3: Inter-Branch Transfer

```
┌──────────────────┐
│ Create Transfer  │
│   (CREATED)      │
└────────┬─────────┘
         │
         ▼
┌──────────────────┐
│ Dispatch Transfer│
│  (IN_TRANSIT)    │
└────────┬─────────┘
         │
         ├──► Create Issue at Source Branch
         │    └─► IssueDetail
         │    └─► FIFO Consumption
         │    └─► Ledger: TRANSFER_OUT
         │    └─► Update Sender Stock (decrease)
         │
         └──► Update Transfer:
              sender_issue_id = {issueId}
              status = IN_TRANSIT

         ▼
┌──────────────────┐
│ Receive Transfer │
│   (RECEIVED)     │
└────────┬─────────┘
         │
         ├──► Create GRN at Destination Branch
         │    └─► GrnDetail
         │    └─► Check Approval (if variance)
         │    └─► Ledger: TRANSFER_IN
         │    └─► Update Receiver Stock (increase)
         │
         └──► Update Transfer:
              receiver_grn_id = {grnId}
              status = RECEIVED
```

**Ledger Entries Created:**

| Stage | Branch | Transaction | Qty In | Qty Out | TXN_TYPE |
|-------|--------|-------------|--------|---------|----------|
| Dispatch | BR001 (source) | Issue #2002 | 0 | 200 | TRANSFER_OUT |
| Receive | BR002 (dest) | GRN #1003 | 200 | 0 | TRANSFER_IN |

---

### Workflow 4: Price Variance Approval

```
┌─────────────────────┐
│ GRN Submitted       │
└──────┬──────────────┘
       │
       ▼
┌────────────────────────────┐
│ Get Last GRN Rate          │
│ (from ledger for same      │
│  branch + item)            │
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ Calculate Variance %       │
│ = (New - Last) / Last * 100│
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ Check Approval Rules       │
│ (match txn_type + variance)│
└──────┬─────────────────────┘
       │
       ├── No Rule Match ──► Auto-Approve
       │
       ▼
┌────────────────────────────┐
│ Route to Required Role     │
│ (from approval rule)       │
└──────┬─────────────────────┘
       │
       ▼
┌────────────────────────────┐
│ Status = PENDING_APPROVAL  │
│ Notify: required_role      │
└────────────────────────────┘
```

**Example:**

```
Item: Full Cream Milk
Last GRN Rate: ₹45/unit
New GRN Rate: ₹52/unit

Variance:
= (₹52 - ₹45) / ₹45 * 100
= 15.56%

Approval Rules:
- Rule 1: 0-5% → Auto-approve
- Rule 2: 5-10% → PURCHASE_MANAGER
- Rule 3: 10%+ → FINANCE_HEAD

Result:
✅ Variance 15.56% matches Rule 3
✅ Required Role: FINANCE_HEAD
✅ Status: PENDING_APPROVAL
✅ Notify Finance Head
```

---

## 📊 Reporting

### Report Categories

1. **Inventory Reports**
   - Current Stock Report
   - Consolidated Stock Summary
   - Stock Ledger (Audit Trail)
   - Stock Aging Report
   - Non-Moving Stock Report

2. **Transaction Reports**
   - GRN Summary Report
   - FIFO Consumption Report
   - Issue to Production Report
   - Inter-Branch Transfer Report

3. **Financial Reports**
   - Price Variance Report
   - GRN vs Invoice Comparison (3-Way Match)
   - PO vs GRN Report

4. **Analytical Reports**
   - Supplier Performance Report
   - Audit & Exception Report

### Multi-Branch Reporting

All reports support:
- **Single Branch**: `branchIds=BR001`
- **Multiple Branches**: `branchIds=BR001,BR002,BR003`
- **All Branches**: `branchIds=` (empty or null)

### Report Filtering

Common filters across reports:
- **Date Range**: `fromDate`, `toDate`
- **Branch**: `branchIds` (comma-separated)
- **Item**: `itemId`
- **Supplier**: `suppId`
- **Status**: `status`
- **Variance Threshold**: `minVariancePct`
- **Aging Threshold**: `agingDays`

---

## 🔒 Security Considerations

### Current Implementation
- No authentication/authorization implemented
- Public REST endpoints
- Assumes trusted network

### Recommended Enhancements
1. **Spring Security** integration
2. **JWT-based authentication**
3. **Role-based access control (RBAC)**
4. **API rate limiting**
5. **Input validation & sanitization**
6. **SQL injection prevention** (using JPA PreparedStatements)

---

## 🚨 Exception Handling

### Custom Exceptions

#### MmsException (Base)
```java
public class MmsException extends RuntimeException {
    private String errorCode;

    public MmsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

#### InsufficientStockException
```java
public class InsufficientStockException extends MmsException {
    private String itemId;
    private String locationId;
    private BigDecimal requestedQty;
    private BigDecimal availableQty;

    public InsufficientStockException(String itemId, BigDecimal requested,
                                      BigDecimal available) {
        super(String.format("Insufficient stock for item %s. Requested: %s, Available: %s",
            itemId, requested, available), "INSUFFICIENT_STOCK");
        // ...
    }
}
```

#### ApprovalRequiredException
```java
public class ApprovalRequiredException extends MmsException {
    private String txnType;
    private List<PriceVarianceInfo> variances;

    public ApprovalRequiredException(String txnType,
                                     List<PriceVarianceInfo> variances) {
        super("Approval required due to price variance", "APPROVAL_REQUIRED");
        // ...
    }
}
```

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MmsException.class)
    public ResponseEntity<ApiResponse<Void>> handleMmsException(MmsException ex) {
        return ResponseEntity.badRequest()
            .body(ApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleInsufficientStock(
            InsufficientStockException ex) {
        Map<String, Object> details = Map.of(
            "itemId", ex.getItemId(),
            "requestedQty", ex.getRequestedQty(),
            "availableQty", ex.getAvailableQty()
        );
        return ResponseEntity.status(409)  // Conflict
            .body(ApiResponse.error(ex.getMessage(), details));
    }

    @ExceptionHandler(ApprovalRequiredException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleApprovalRequired(
            ApprovalRequiredException ex) {
        Map<String, Object> details = Map.of(
            "txnType", ex.getTxnType(),
            "variances", ex.getVariances()
        );
        return ResponseEntity.status(412)  // Precondition Required
            .body(ApiResponse.error(ex.getMessage(), details));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        return ResponseEntity.status(500)
            .body(ApiResponse.error("Internal server error: " + ex.getMessage()));
    }
}
```

---

## 📝 Development Guidelines

### Code Style
- Use **Lombok** annotations (`@Builder`, `@Getter`, `@Setter`)
- Follow **Spring Boot best practices**
- Use **constructor injection** for dependencies
- Keep services **stateless**
- Use `@Transactional` for write operations

### Testing Strategy
- Unit tests for services
- Integration tests for repositories
- End-to-end tests for critical workflows

### Database Migrations
- **Never modify existing Flyway migrations**
- Create new V7, V8, etc. for schema changes
- Test migrations on dev before production

---

## 🔧 Troubleshooting

### Common Issues

#### 1. Java Version Mismatch
```bash
# Current: Java 11 (sdkman)
# Required: Java 17+

# Fix:
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem
mvn clean compile
```

#### 2. Flyway Migration Failure
```bash
# Check migration history
mysql> SELECT * FROM flyway_schema_history;

# Repair if needed (caution!)
mvn flyway:repair
```

#### 3. Connection Pool Exhaustion
```yaml
# application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20  # Increase if needed
      connection-timeout: 30000
```

#### 4. AWS S3 Access Denied
```bash
# Verify credentials
export AWS_ACCESS_KEY=your_key
export AWS_SECRET_KEY=your_secret

# Test S3 access
aws s3 ls s3://mms-invoices/
```

---

## 📚 Additional Resources

### Project Files
- **CLAUDE.md**: Quick reference guide
- **pom.xml**: Maven dependencies
- **application.yml**: Configuration
- **Flyway migrations**: `src/main/resources/db/migration/`

### Key Technologies Documentation
- [Spring Boot 3.2.x](https://docs.spring.io/spring-boot/docs/3.2.x/reference/html/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [Flyway](https://flywaydb.org/documentation/)
- [AWS SDK for Java 2.x](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/)

---

## 🎓 Key Takeaways

### System Strengths
✅ **Append-only ledger** ensures audit trail integrity
✅ **FIFO compliance** with full batch traceability
✅ **Multi-branch support** with consolidated reporting
✅ **Approval workflows** for cost control
✅ **RESTful API design** with consistent error handling
✅ **AWS S3 integration** for document storage

### Design Principles
1. **Single Source of Truth**: Ledger is immutable
2. **Transactional Integrity**: All workflows wrapped in `@Transactional`
3. **FIFO Enforcement**: Automatic batch consumption
4. **Price Variance Control**: Configurable approval thresholds
5. **Separation of Concerns**: Clear layered architecture

---

## 📞 Support

For issues or questions:
1. Check this documentation
2. Review CLAUDE.md for quick reference
3. Inspect Flyway migrations for schema details
4. Debug with SQL queries on ledger tables

---

**Document Version**: 1.0
**Last Updated**: 2026-02-12
**System Version**: MMS v0.0.1-SNAPSHOT

---

**End of Documentation**