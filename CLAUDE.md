# Material Management System (MMS)

## Tech Stack
- **Spring Boot 3.2.1**, Java 17, Maven
- **MySQL 8.0** with Flyway migrations (V1-V6)
- **Spring Data JPA** / Hibernate
- **Lombok** for boilerplate
- **AWS SDK v2** for S3 (invoice PDFs)
- Context path: `/mms`, Port: `8080`

## Build & Run
```bash
# Requires Java 17+
mvn compile
mvn spring-boot:run
# Note: Local machine has Java 11 (sdkman) - need Java 17 to compile
```

## Project Structure
```
src/main/java/com/countrydelight/mms/
├── config/                     # S3Config
├── controller/
│   ├── master/                 # 8 master CRUD controllers (Branch, Group, Unit, Supplier, Item, Location, Role, ApprovalRule)
│   ├── GrnController           # /api/v1/grn
│   ├── IssueController         # /api/v1/issue
│   ├── TransferController      # /api/v1/transfer
│   ├── StockController         # /api/v1/stock
│   ├── InvoiceController       # /api/v1/invoice
│   └── ReportController        # /api/v1/report
├── dto/
│   ├── common/                 # ApiResponse<T>
│   ├── inward/                 # GrnCreateRequest, GrnResponse, PriceSuggestionResponse
│   ├── outward/                # IssueCreateRequest, IssueResponse
│   ├── transfer/               # TransferCreateRequest
│   ├── stock/                  # StockSummaryResponse, AgingReportResponse
│   └── report/                 # 14 report DTOs + ReportFilterDTO
├── entity/
│   ├── master/                 # 10 master entities + 2 composite ID classes
│   ├── inward/                 # GrnHeader, GrnDetail
│   ├── outward/                # IssueHeader, IssueDetail, IssueFifoConsumption
│   ├── purchase/               # PoHeader, PoDetail, SupplierInvoice
│   ├── transfer/               # StockTransferHeader, StockTransferDetail
│   └── stock/                  # MaterialStockLedger, BranchMaterialStock
├── repository/
│   ├── master/                 # 10 repositories
│   ├── inward/, outward/, purchase/, transfer/, stock/
│   └── report/                 # ReportRepository
├── service/
│   ├── master/                 # 8 master services
│   ├── inward/                 # GrnService
│   ├── outward/                # IssueService, FifoService
│   ├── transfer/               # TransferService
│   ├── purchase/               # InvoiceService
│   ├── approval/               # ApprovalService
│   └── stock/                  # StockLedgerService, AgingService, ReportService
└── exception/                  # MmsException, InsufficientStockException, ApprovalRequiredException, GlobalExceptionHandler
```

## Master Tables & Their APIs
All under `/api/v1/master/`:

| Entity | Endpoint | Filters | ID Type |
|--------|----------|---------|---------|
| BranchMaster | `/branches` | `?name=` | String (branch_id) |
| GroupMaster | `/groups` | `?desc=` | String (group_id) |
| SubGroupMaster | `/groups/{groupId}/sub-groups` | by groupId | Composite (group_id, sub_group_id) |
| UnitMaster | `/units` | `?desc=` | String (unit_id) |
| SupplierMaster | `/suppliers` | `?name=` | String (supp_id) |
| ItemMaster | `/items` | `?name=&groupId=&subGroupId=&suppId=` | String (item_id) |
| LocationMaster | `/locations` | `?branchId=&parentId=` | String (location_id) |
| RoleMaster | `/roles` | `?name=` | String (role_id) |
| UserRoleMap | `/roles/user-roles` | `?userId=` | Composite (user_id, role_id) |
| ApprovalRule | `/approval-rules` | `?txnType=&conditionType=` | String (rule_id) |

## Key Entity Relationships
- **ItemMaster** -> SubGroupMaster (group_id + sub_group_id), SupplierMaster, UnitMaster
- **LocationMaster** -> BranchMaster, self-referencing parent (hierarchy: warehouse > shelf > bin)
- **SubGroupMaster** -> GroupMaster (composite key: group_id + sub_group_id)
- **UserRoleMap** -> RoleMaster (composite key: user_id + role_id)
- **ApprovalRule** -> RoleMaster (required_role)

## Key Patterns
- All IDs for master tables are user-provided Strings (max 20 chars), NOT auto-generated
- `created_at` / `updated_at` are DB-managed (insertable=false, updatable=false)
- `ApiResponse<T>` wrapper for all REST responses
- `MmsException` for business errors, handled by `GlobalExceptionHandler`
- Services use `@Transactional` for write operations
- Entities use Lombok `@Builder`, `@Getter`, `@Setter`
- Lazy-loaded relationships with `insertable=false, updatable=false` on join columns

## Transactional Flow
1. **GRN (Goods Receipt):** DRAFT -> submit -> PENDING_APPROVAL / auto-POST -> ledger entry
2. **Issue (Material Out):** DRAFT -> post -> FIFO consumption from GRN batches -> ledger entry
3. **Transfer:** CREATE -> dispatch (Issue from source) -> IN_TRANSIT -> receive (GRN at dest)
4. **Ledger:** Append-only `MaterialStockLedger` is single source of truth
5. **Approval:** Rules check price/qty variance thresholds

## URLs & Endpoints
Base URL: `http://<host>:8080/mms`

| URL | Description |
|-----|-------------|
| `/mms/swagger-ui.html` | Swagger UI — interactive API docs |
| `/mms/v3/api-docs` | OpenAPI 3.0 JSON spec |
| `/mms/db-risk-guard` | DB Risk Guard Web UI (self-service SQL risk analysis) |
| `/mms/api/v1/master/*` | Master data CRUD (see table below) |
| `/mms/api/v1/grn/*` | GRN (Goods Receipt) operations |
| `/mms/api/v1/issue/*` | Issue (Material Out) operations |
| `/mms/api/v1/transfer/*` | Inter-branch transfer operations |
| `/mms/api/v1/stock/*` | Stock queries, FIFO, aging |
| `/mms/api/v1/invoice/*` | Supplier invoice upload (S3) |
| `/mms/api/v1/report/*` | Reports (stock summary, ledger, variance) |

QA Server: `http://non-prod-apps.countrydelight.in:8080/mms`

## Database
- Flyway migrations in `src/main/resources/db/migration/`
- V1: master tables, V2: purchase, V3: inward, V4: outward, V5: transfer, V6: stock
- MySQL connection via env vars in application.yml
