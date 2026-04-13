# Material Management System (MMS)

An ERP-grade Material Management System built with Spring Boot, implementing strict inventory control with FIFO valuation, approval workflows, and inter-branch transfers.

## Tech Stack

- Java 17
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- MySQL
- Flyway (Database Migrations)
- AWS S3 (Invoice Storage)
- Lombok
- Maven

## Core System Rules (Non-Negotiable)

1. **Ledger is the single source of truth** - All stock movements are recorded in `material_stock_ledger`
2. **GRN is the ONLY way to increase stock** - Stock can only enter the system through Goods Receipt Notes
3. **FIFO is mandatory for issue** - When issuing stock, oldest batches are consumed first
4. **Aging is derived, never stored** - Stock aging is calculated from ledger transaction dates
5. **Invoices never update stock directly** - Invoices are reference documents; stock updates only through GRN
6. **Inter-branch transfer requires sender issue + receiver GRN** - Two-step process for stock movement between branches
7. **Approval workflow is enforced** - Price variances and other conditions trigger approval requirements
8. **No direct update/delete on stock ledger** - Ledger is append-only (INSERT operations only)

## Project Structure

```
com.countrydelight.mms
├── config/           # Configuration classes (S3, etc.)
├── controller/       # REST API controllers
├── dto/              # Data Transfer Objects
│   ├── common/       # Common DTOs (ApiResponse)
│   ├── inward/       # GRN-related DTOs
│   ├── outward/      # Issue-related DTOs
│   ├── transfer/     # Transfer-related DTOs
│   └── stock/        # Stock & Aging DTOs
├── entity/           # JPA Entities
│   ├── master/       # Master data entities
│   ├── purchase/     # PO and Invoice entities
│   ├── inward/       # GRN entities
│   ├── outward/      # Issue entities
│   ├── transfer/     # Transfer entities
│   └── stock/        # Ledger and Stock entities
├── repository/       # Spring Data JPA Repositories
├── service/          # Business Logic Services
│   ├── master/       # Master data services
│   ├── purchase/     # Invoice services
│   ├── inward/       # GRN services
│   ├── outward/      # Issue services
│   ├── transfer/     # Transfer services
│   ├── stock/        # Stock, FIFO, Aging services
│   └── approval/     # Approval workflow service
├── exception/        # Custom exceptions
└── util/             # Utility classes
```

## Key Features

### 1. FIFO (First-In-First-Out) Stock Consumption

When issuing stock, the system automatically:
- Retrieves available GRN batches ordered by date (oldest first)
- Consumes stock from oldest batch until quantity is fulfilled
- Records consumption details in `issue_fifo_consumption` table
- Calculates weighted average rate from consumed batches
- Updates `qty_remaining` in GRN details for tracking

```java
// Example: Issue 100 units
// Batch 1 (GRN-001, oldest): 60 units @ ₹10 → consume all
// Batch 2 (GRN-002, newer): 50 units @ ₹12 → consume 40
// Weighted Avg Rate = (60×10 + 40×12) / 100 = ₹10.80
```

### 2. GRN Posting Logic

GRN (Goods Receipt Note) is the **only** way to add stock:

1. **Create GRN** - Draft status, no stock impact
2. **Submit for Approval** - System checks price variance against last GRN rate
3. **Approval Check**:
   - Fetches last GRN rate from ledger for the item/branch
   - Calculates variance percentage
   - If variance exceeds threshold → requires approval
   - If within threshold → auto-posts
4. **Post GRN** - Updates:
   - `material_stock_ledger` (INSERT record)
   - `branch_material_stock` (UPDATE qty and avg cost)
   - `grn_detail.qty_remaining` (for FIFO tracking)
   - `po_detail.qty_received` (if linked to PO)

### 3. Pricing Logic

**Price source hierarchy:**
1. **Last GRN rate from ledger** (primary source)
2. **Item master cost price** (fallback if no GRN history)

**Price variance workflow:**
1. System auto-fetches last GRN rate when creating GRN
2. User can modify price based on supplier invoice
3. On submit, system calculates variance percentage
4. If variance exceeds approval threshold → status = `PENDING_APPROVAL`
5. Authorized approver must approve before posting
6. Final accepted price is stored in GRN and ledger

### 4. Stock Aging

Aging is **derived** from ledger `txn_date`, never stored:

**Aging Buckets:**
- 0-30 days
- 31-60 days
- 61-90 days
- 90+ days

The aging report:
- Iterates through GRN batches with remaining stock
- Calculates days since GRN date
- Categorizes into appropriate buckets
- Returns quantity and value per bucket

### 5. Approval Workflow

Approval rules are defined in `approval_rule` table:

| Field | Description |
|-------|-------------|
| txn_type | GRN / ISSUE / TRANSFER |
| condition_type | PRICE_VARIANCE / QTY_VARIANCE |
| threshold_value | Percentage threshold |
| required_role | Role that can approve |

Example rule: "GRN with price variance > 5% requires MANAGER approval"

### 6. Inter-Branch Transfer

Transfer involves three steps:

1. **CREATE** - Transfer request created
   - Status: `CREATED`
   - Validates stock availability at source

2. **DISPATCH** - Sender branch dispatches
   - Creates Issue at sender branch
   - FIFO consumption at source location
   - Ledger entry: `TRANSFER_OUT`
   - Status: `IN_TRANSIT`

3. **RECEIVE** - Receiver branch receives
   - Creates GRN at receiver branch
   - Ledger entry: `TRANSFER_IN`
   - Transfer rate preserved from sender FIFO
   - Status: `RECEIVED`

## API Endpoints

### GRN APIs
```
GET  /mms/api/v1/grn/price-suggestion?branchId=&itemId=
POST /mms/api/v1/grn
POST /mms/api/v1/grn/{grnId}/submit?submittedBy=
POST /mms/api/v1/grn/{grnId}/approve?approvedBy=
GET  /mms/api/v1/grn/{grnId}
GET  /mms/api/v1/grn/branch/{branchId}
GET  /mms/api/v1/grn/pending-approval
```

### Issue APIs
```
POST /mms/api/v1/issue
POST /mms/api/v1/issue/{issueId}/post?postedBy=
GET  /mms/api/v1/issue/{issueId}
GET  /mms/api/v1/issue/branch/{branchId}
GET  /mms/api/v1/issue/check-stock?branchId=&itemId=&locationId=&qty=
```

### Transfer APIs
```
POST /mms/api/v1/transfer
POST /mms/api/v1/transfer/{transferId}/dispatch?sourceLocationId=&dispatchedBy=
POST /mms/api/v1/transfer/{transferId}/receive?destLocationId=&receivedBy=
GET  /mms/api/v1/transfer/{transferId}
GET  /mms/api/v1/transfer/pending-dispatch/{branchId}
GET  /mms/api/v1/transfer/pending-receipt/{branchId}
```

### Stock APIs
```
GET /mms/api/v1/stock/summary/{branchId}
GET /mms/api/v1/stock/balance?branchId=&itemId=&locationId=
GET /mms/api/v1/stock/aging/{branchId}?asOfDate=
```

### Invoice APIs
```
POST /mms/api/v1/invoice (multipart/form-data)
POST /mms/api/v1/invoice/{invoiceId}/upload
GET  /mms/api/v1/invoice/{invoiceId}
GET  /mms/api/v1/invoice/supplier/{suppId}
GET  /mms/api/v1/invoice/by-date?startDate=&endDate=
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| DB_HOST | MySQL host | localhost |
| DB_PORT | MySQL port | 3306 |
| DB_NAME | Database name | mms_db |
| DB_USERNAME | Database username | root |
| DB_PASSWORD | Database password | password |
| AWS_ACCESS_KEY | AWS access key | - |
| AWS_SECRET_KEY | AWS secret key | - |
| AWS_REGION | AWS region | ap-south-1 |
| AWS_S3_BUCKET | S3 bucket name | mms-invoices |
| SERVER_PORT | Server port | 8080 |

### Database Setup

1. Create MySQL database:
```sql
CREATE DATABASE mms_db;
```

2. Run the application - Flyway will execute migrations automatically

### Running the Application

```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# With environment variables
export DB_HOST=your-db-host
export DB_PASSWORD=your-password
mvn spring-boot:run
```

## Database Schema

### Master Tables
- `branch_master` - Branch information
- `group_master` - Item groups
- `sub_group_master` - Item sub-groups
- `unit_master` - Units of measure
- `manufacturer_master` - Manufacturers
- `supplier_master` - Suppliers
- `item_master` - Items/materials
- `location_master` - Storage locations
- `role_master` - User roles
- `user_role_map` - User-role mapping
- `approval_rule` - Approval rules

### Transaction Tables
- `po_header` / `po_detail` - Purchase Orders
- `supplier_invoice` - Supplier invoices
- `grn_header` / `grn_detail` - Goods Receipt Notes
- `issue_header` / `issue_detail` - Material Issues
- `issue_fifo_consumption` - FIFO consumption tracking
- `stock_transfer_header` / `stock_transfer_detail` - Inter-branch transfers

### Stock Tables
- `material_stock_ledger` - Single source of truth (append-only)
- `branch_material_stock` - Denormalized stock summary

## License

Proprietary - Country Delight
