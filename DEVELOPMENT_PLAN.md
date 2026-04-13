# Material Management System - Development Plan
## Step-by-Step Guide to Get Started

---

## 📊 Current Status Summary

### ✅ FULLY IMPLEMENTED (95% Complete)
- **All Controllers** (15) - Complete with REST endpoints
- **All Services** (18) - Full business logic implemented
- **All Entities** (30) - Complete with relationships
- **All Repositories** (24) - Custom queries implemented
- **Database Migrations** (V1-V6) - Complete schema
- **Core Features**:
  - GRN workflow with approval
  - Issue with FIFO consumption
  - Inter-branch transfers (3-phase)
  - Stock ledger (append-only)
  - 14 comprehensive reports
  - AWS S3 invoice storage

### ⚠️ MISSING/NEEDS WORK
- **Tests** - No unit/integration tests
- **Swagger Documentation** - Partial implementation
- **Authentication** - Not implemented
- **Advanced Features** - Excel export, batch import, etc.

---

## 🎯 Development Roadmap

We'll follow this sequence:

### Phase 1: Environment Setup & Verification (1-2 hours)
- Install Java 17
- Setup MySQL database
- Configure environment variables
- Build & run application
- Verify Flyway migrations

### Phase 2: Manual API Testing (2-3 hours)
- Create master data (branches, items, suppliers)
- Test GRN workflow
- Test Issue workflow
- Test Transfer workflow
- Test Reports

### Phase 3: Add Swagger Documentation (2-4 hours)
- Configure Swagger/OpenAPI
- Add annotations to all controllers
- Test documentation UI

### Phase 4: Add Unit Tests (1-2 weeks)
- Service layer unit tests
- Repository integration tests
- Controller tests
- FIFO logic tests

### Phase 5: Advanced Features (Optional)
- Excel/PDF export
- Batch import
- Authentication/Authorization
- Audit logging

---

## 📋 PHASE 1: Environment Setup

### Step 1: Check Java Version

```bash
# Check current Java version
java -version
# Current: Java 11 (needs Java 17)

# Install Java 17 using SDKMAN
sdk install java 17.0.9-tem
sdk use java 17.0.9-tem

# Verify
java -version
# Should show: openjdk version "17.0.9"
```

### Step 2: Setup MySQL Database

```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE mms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Create user (optional)
CREATE USER 'mms_user'@'localhost' IDENTIFIED BY 'mms_password';
GRANT ALL PRIVILEGES ON mms_db.* TO 'mms_user'@'localhost';
FLUSH PRIVILEGES;

# Verify
SHOW DATABASES;
USE mms_db;
```

### Step 3: Setup AWS S3 (Optional for Invoice Storage)

```bash
# If you have AWS credentials, set them
export AWS_ACCESS_KEY=your_access_key_here
export AWS_SECRET_KEY=your_secret_key_here

# Or skip for now - invoice upload will fail but rest will work
```

### Step 4: Configure Environment Variables

```bash
# Create .env file (or export in terminal)
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=mms_db
export DB_USERNAME=root
export DB_PASSWORD=your_mysql_password

# Optional: AWS credentials
export AWS_ACCESS_KEY=your_key
export AWS_SECRET_KEY=your_secret
```

### Step 5: Build the Application

```bash
# Navigate to project directory
cd /home/vipin/workspace/cd-material-management

# Clean and compile
mvn clean compile

# Expected output:
# [INFO] BUILD SUCCESS
```

### Step 6: Run Flyway Migrations

```bash
# Run application (Flyway will auto-migrate on startup)
mvn spring-boot:run

# Watch console output for:
# - Flyway migration V1-V6 executed
# - Application started on port 8080
# - Context path: /mms
```

### Step 7: Verify Database Tables

```sql
-- Login to MySQL
mysql -u root -p mms_db

-- Check Flyway history
SELECT version, description, installed_on, success
FROM flyway_schema_history
ORDER BY installed_rank;

-- Expected output:
-- V1: init_master_tables
-- V2: purchase_tables
-- V3: inward_tables
-- V4: outward_tables
-- V5: transfer_tables
-- V6: stock_tables

-- Verify tables created
SHOW TABLES;

-- Should show 24 tables:
-- branch_master, group_master, sub_group_master, unit_master,
-- manufacturer_master, supplier_master, item_master, location_master,
-- role_master, user_role_map, approval_rule_master,
-- po_header, po_detail, supplier_invoice,
-- grn_header, grn_detail,
-- issue_header, issue_detail, issue_fifo_consumption,
-- stock_transfer_header, stock_transfer_detail,
-- material_stock_ledger, branch_material_stock,
-- flyway_schema_history
```

### Step 8: Verify Application Running

```bash
# Application should be running on:
# http://localhost:8080/mms

# Test health check (if you add actuator later)
curl http://localhost:8080/mms/actuator/health
```

---

## 📋 PHASE 2: Manual API Testing

### Step 1: Create Master Data (Order Matters!)

Use **Postman**, **curl**, or **HTTPie** for testing.

#### 1.1 Create Branch
```bash
curl -X POST http://localhost:8080/mms/api/v1/master/branches \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "branchName": "Delhi Warehouse",
    "address": "Sector 18, Noida",
    "contactPerson": "Raj Kumar",
    "contactNumber": "9876543210",
    "active": true
  }'

# Expected: HTTP 200 with success message

# Create another branch for transfers
curl -X POST http://localhost:8080/mms/api/v1/master/branches \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR002",
    "branchName": "Mumbai Warehouse",
    "address": "Andheri East",
    "contactPerson": "Priya Sharma",
    "contactNumber": "9876543211",
    "active": true
  }'
```

#### 1.2 Create Location (Storage Hierarchy)
```bash
# Warehouse level
curl -X POST http://localhost:8080/mms/api/v1/master/locations \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "WH01",
    "locationName": "Main Warehouse",
    "branchId": "BR001",
    "locationType": "WAREHOUSE",
    "active": true
  }'

# Shelf level (child of warehouse)
curl -X POST http://localhost:8080/mms/api/v1/master/locations \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "SHELF01",
    "locationName": "Shelf A",
    "branchId": "BR001",
    "parentLocationId": "WH01",
    "locationType": "SHELF",
    "active": true
  }'

# Bin level (child of shelf)
curl -X POST http://localhost:8080/mms/api/v1/master/locations \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "BIN001",
    "locationName": "Bin A1",
    "branchId": "BR001",
    "parentLocationId": "SHELF01",
    "locationType": "BIN",
    "capacity": 500,
    "active": true
  }'
```

#### 1.3 Create Group & Sub-Group
```bash
# Group
curl -X POST http://localhost:8080/mms/api/v1/master/groups \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP01",
    "groupDesc": "Dairy Products",
    "active": true
  }'

# Sub-Group
curl -X POST http://localhost:8080/mms/api/v1/master/groups/GRP01/sub-groups \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP01",
    "subGroupId": "SG01",
    "subGroupDesc": "Fresh Milk",
    "active": true
  }'
```

#### 1.4 Create Unit
```bash
curl -X POST http://localhost:8080/mms/api/v1/master/units \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "LTR",
    "unitDesc": "Litre",
    "active": true
  }'
```

#### 1.5 Create Manufacturer
```bash
curl -X POST http://localhost:8080/mms/api/v1/master/manufacturers \
  -H "Content-Type: application/json" \
  -d '{
    "manufId": "MF001",
    "manufName": "Amul Dairy",
    "address": "Gujarat",
    "contactNumber": "9876543212",
    "active": true
  }'
```

#### 1.6 Create Supplier
```bash
curl -X POST http://localhost:8080/mms/api/v1/master/suppliers \
  -H "Content-Type: application/json" \
  -d '{
    "suppId": "SUP001",
    "suppName": "Fresh Foods Supplier",
    "address": "Delhi",
    "contactPerson": "Amit Verma",
    "contactNumber": "9876543213",
    "gstNo": "27AAAAA1234A1Z5",
    "active": true
  }'
```

#### 1.7 Create Item
```bash
curl -X POST http://localhost:8080/mms/api/v1/master/items \
  -H "Content-Type: application/json" \
  -d '{
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
  }'
```

#### 1.8 Create Role & Approval Rule
```bash
# Role
curl -X POST http://localhost:8080/mms/api/v1/master/roles \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "PM",
    "roleName": "Purchase Manager",
    "active": true
  }'

# Approval Rule (5-10% variance needs PM approval)
curl -X POST http://localhost:8080/mms/api/v1/master/approval-rules \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "APR001",
    "txnType": "GRN",
    "conditionType": "PRICE_VARIANCE",
    "minValue": 5.0,
    "maxValue": 10.0,
    "requiredRole": "PM",
    "active": true
  }'
```

### Step 2: Test GRN Workflow

#### 2.1 Create GRN (Draft)
```bash
curl -X POST http://localhost:8080/mms/api/v1/grn \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "suppId": "SUP001",
    "grnDate": "2026-02-12",
    "remarks": "First delivery",
    "details": [
      {
        "itemId": "ITM001",
        "unitId": "LTR",
        "locationId": "BIN001",
        "qtyReceived": 500,
        "rate": 45.00,
        "remarks": "Good quality"
      }
    ]
  }'

# Expected:
# {
#   "success": true,
#   "message": "GRN created successfully",
#   "data": {
#     "grnId": 1,
#     "status": "DRAFT",
#     ...
#   }
# }
```

#### 2.2 Submit GRN (Should Auto-Post)
```bash
# First GRN has no price history, so should auto-post
curl -X POST http://localhost:8080/mms/api/v1/grn/1/submit

# Expected:
# "status": "POSTED"
# Stock ledger entry created
```

#### 2.3 Verify Stock Ledger
```sql
-- In MySQL
SELECT * FROM material_stock_ledger WHERE item_id = 'ITM001';

-- Expected:
-- ledger_id=1, branch_id=BR001, item_id=ITM001,
-- txn_type=GRN, qty_in=500, balance_qty=500
```

#### 2.4 Create Second GRN with Price Variance
```bash
curl -X POST http://localhost:8080/mms/api/v1/grn \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "suppId": "SUP001",
    "grnDate": "2026-02-13",
    "details": [
      {
        "itemId": "ITM001",
        "unitId": "LTR",
        "locationId": "BIN001",
        "qtyReceived": 300,
        "rate": 48.00
      }
    ]
  }'

# Submit this GRN
curl -X POST http://localhost:8080/mms/api/v1/grn/2/submit

# Expected:
# If variance > 5%: "status": "PENDING_APPROVAL"
# Variance = (48-45)/45*100 = 6.67% (requires PM approval)
```

#### 2.5 Approve GRN
```bash
curl -X POST "http://localhost:8080/mms/api/v1/grn/2/approve?userId=USER001&roleId=PM"

# Expected:
# "status": "POSTED"
# Stock updated: 500 + 300 = 800
```

### Step 3: Test Issue Workflow (FIFO)

#### 3.1 Check Stock Availability
```bash
curl -X GET "http://localhost:8080/mms/api/v1/issue/check-stock?branchId=BR001&itemId=ITM001&locationId=BIN001&qty=600"

# Expected:
# "available": true
# "availableQty": 800
```

#### 3.2 Create Issue (Draft)
```bash
curl -X POST http://localhost:8080/mms/api/v1/issue \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "issueType": "PRODUCTION",
    "issueDate": "2026-02-14",
    "remarks": "Production batch #1",
    "details": [
      {
        "itemId": "ITM001",
        "locationId": "BIN001",
        "qtyIssued": 600
      }
    ]
  }'

# Expected:
# "issueId": 1
# "status": "DRAFT"
```

#### 3.3 Post Issue (FIFO Consumption)
```bash
curl -X POST http://localhost:8080/mms/api/v1/issue/1/post

# Expected:
# "status": "POSTED"
# FIFO consumption:
#   - 500 from GRN 1 @ ₹45
#   - 100 from GRN 2 @ ₹48
# Weighted avg rate = (500*45 + 100*48)/600 = ₹45.50
```

#### 3.4 Verify FIFO Consumption
```sql
SELECT * FROM issue_fifo_consumption WHERE issue_id = 1;

-- Expected 2 rows:
-- grn_id=1, qty_consumed=500, rate=45.00
-- grn_id=2, qty_consumed=100, rate=48.00

SELECT * FROM grn_detail WHERE item_id = 'ITM001';

-- Expected:
-- grn_id=1, qty_remaining=0 (fully consumed)
-- grn_id=2, qty_remaining=200 (300-100)
```

### Step 4: Test Transfer Workflow

#### 4.1 Create Transfer
```bash
curl -X POST http://localhost:8080/mms/api/v1/transfer \
  -H "Content-Type: application/json" \
  -d '{
    "fromBranchId": "BR001",
    "toBranchId": "BR002",
    "transferDate": "2026-02-15",
    "remarks": "Stock rebalancing",
    "details": [
      {
        "itemId": "ITM001",
        "qtyToTransfer": 100,
        "fromLocationId": "BIN001",
        "toLocationId": "BIN101"
      }
    ]
  }'

# Expected:
# "transferId": 1
# "status": "CREATED"
```

#### 4.2 Dispatch Transfer
```bash
curl -X POST http://localhost:8080/mms/api/v1/transfer/1/dispatch

# Expected:
# "status": "IN_TRANSIT"
# "senderIssueId": 2
# Stock reduced at BR001 (200-100=100)
# Ledger entry: TRANSFER_OUT
```

#### 4.3 Receive Transfer
```bash
curl -X POST http://localhost:8080/mms/api/v1/transfer/1/receive \
  -H "Content-Type: application/json" \
  -d '{
    "receivedDate": "2026-02-16",
    "details": [
      {
        "itemId": "ITM001",
        "qtyReceived": 100,
        "locationId": "BIN101"
      }
    ]
  }'

# Expected:
# "status": "RECEIVED"
# "receiverGrnId": 3
# Stock increased at BR002 (0+100=100)
# Ledger entry: TRANSFER_IN
```

### Step 5: Test Stock Queries

#### 5.1 Stock Summary
```bash
curl -X GET http://localhost:8080/mms/api/v1/stock/summary/BR001

# Expected:
# Shows current stock at BR001:
# item_id=ITM001, qty_on_hand=100, avg_cost=45.50
```

#### 5.2 Stock Balance
```bash
curl -X GET "http://localhost:8080/mms/api/v1/stock/balance?branchId=BR001&itemId=ITM001&locationId=BIN001"

# Expected:
# "qtyOnHand": 100
```

#### 5.3 Stock Aging
```bash
curl -X GET "http://localhost:8080/mms/api/v1/stock/aging/BR001?asOfDate=2026-02-16"

# Expected:
# Shows aging buckets:
# - "0-30 days": qty based on recent GRNs
# - Older buckets if applicable
```

### Step 6: Test Reports

#### 6.1 Current Stock Report
```bash
curl -X GET "http://localhost:8080/mms/api/v1/reports/current-stock?branchIds=BR001,BR002"

# Expected:
# Shows stock at both branches with values
```

#### 6.2 Stock Ledger Report
```bash
curl -X GET "http://localhost:8080/mms/api/v1/reports/stock-ledger?branchIds=BR001&fromDate=2026-02-12&toDate=2026-02-16"

# Expected:
# Shows all transactions:
# - GRN entries (qty_in)
# - Issue entries (qty_out)
# - Transfer entries (TRANSFER_IN/OUT)
```

#### 6.3 FIFO Consumption Report
```bash
curl -X GET "http://localhost:8080/mms/api/v1/reports/fifo-consumption?branchIds=BR001&fromDate=2026-02-12&toDate=2026-02-16"

# Expected:
# Shows which GRN batches were consumed by each issue
```

#### 6.4 GRN Summary Report
```bash
curl -X GET "http://localhost:8080/mms/api/v1/reports/grn-summary?branchIds=BR001&fromDate=2026-02-12&toDate=2026-02-16"

# Expected:
# Lists all GRNs with totals
```

---

## 📋 PHASE 3: Add Swagger Documentation

### Step 1: Add Swagger Dependencies

Already in pom.xml (verify):
```xml
<!-- If not present, add: -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

### Step 2: Create OpenAPI Configuration

We'll create this file together.

### Step 3: Add Annotations to Controllers

We'll add `@Tag`, `@Operation`, `@Parameter` annotations to all controllers.

### Step 4: Access Swagger UI

```bash
# After implementation:
http://localhost:8080/mms/swagger-ui.html
```

---

## 📋 PHASE 4: Add Unit Tests

### Test Categories to Implement

1. **Service Unit Tests**
   - GrnServiceTest
   - IssueServiceTest
   - FifoServiceTest
   - TransferServiceTest
   - ApprovalServiceTest
   - StockLedgerServiceTest

2. **Repository Integration Tests**
   - Use @DataJpaTest
   - Test custom queries

3. **Controller Tests**
   - Use MockMvc
   - Test REST endpoints

4. **Business Logic Tests**
   - FIFO consumption scenarios
   - Price variance scenarios
   - Approval workflow scenarios

---

## 📋 PHASE 5: Advanced Features (Optional)

1. **Excel/PDF Export**
   - Add Apache POI dependency
   - Create export service
   - Add export endpoints to ReportController

2. **Batch Import**
   - CSV upload for master data
   - Excel upload with validation

3. **Authentication**
   - Spring Security
   - JWT tokens
   - Role-based access control

4. **Audit Logging**
   - Log all changes to master data
   - Log all transactions
   - User activity tracking

5. **Advanced Features**
   - Email notifications for approvals
   - SMS alerts for stock levels
   - Dashboard with charts
   - Scheduled jobs for aging reports

---

## 🎯 Immediate Next Steps

### What We'll Do First:

1. **Verify Environment Setup** (15 mins)
   - Check Java version
   - Setup MySQL
   - Run application
   - Verify migrations

2. **Test Core Workflows** (30-45 mins)
   - Create master data
   - Test GRN flow
   - Test Issue flow
   - Test one report

3. **Add Swagger Documentation** (1-2 hours)
   - Configure OpenAPI
   - Annotate controllers
   - Test documentation UI

4. **Start Writing Tests** (ongoing)
   - Begin with service tests
   - Add repository tests
   - Add controller tests

---

## 📝 Development Commands Cheat Sheet

```bash
# Build
mvn clean compile

# Run
mvn spring-boot:run

# Run with specific profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run tests
mvn test

# Package JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests

# Check dependencies
mvn dependency:tree

# Format code
mvn spotless:apply

# Check for updates
mvn versions:display-dependency-updates
```

---

## 🐛 Troubleshooting

### Application Won't Start
- Check Java version (needs 17)
- Check MySQL is running
- Check DB credentials in application.yml
- Check Flyway migration errors in logs

### Flyway Migration Failed
```sql
-- Check migration status
SELECT * FROM flyway_schema_history;

-- If needed, repair
mvn flyway:repair
```

### Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>

# Or change port in application.yml
server.port: 8081
```

### Database Connection Timeout
- Increase HikariCP timeout in application.yml
- Check MySQL max connections

---

## 📞 Ready to Start?

**Let's begin with Phase 1!**

Tell me:
1. Do you already have Java 17 installed?
2. Is MySQL 8.0 running?
3. Do you have AWS credentials (optional)?

Then we'll:
1. ✅ Setup environment
2. ✅ Run application
3. ✅ Test APIs manually
4. ✅ Add Swagger docs
5. ✅ Write tests

---

**Next Command:** Let's start by checking your Java version:
```bash
java -version
```