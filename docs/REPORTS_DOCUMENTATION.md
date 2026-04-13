# Material Management System - Reports Documentation

> **Version:** 1.0
> **Last Updated:** February 2026
> **Audience:** Operations Team, Finance, Auditors, Developers

---

## Overview

The MMS Reports module provides 14 comprehensive reports for inventory visibility, auditing, and operational decision-making. All reports support:

- **Single Branch View:** Filter by one branch
- **Multi-Branch View:** Filter by multiple branches
- **Company-Wide View:** No filter = ALL branches

---

## Quick Reference: All Reports

| # | Report Name | Endpoint | Primary Users |
|---|-------------|----------|---------------|
| 1 | Current Stock | `/api/v1/reports/current-stock` | Store, Operations |
| 2 | Consolidated Stock | `/api/v1/reports/consolidated-stock` | Management, Finance |
| 3 | Stock Ledger | `/api/v1/reports/stock-ledger` | Audit, Finance |
| 4 | Stock Aging | `/api/v1/reports/stock-aging` | Operations, Finance |
| 5 | FIFO Consumption | `/api/v1/reports/fifo-consumption` | Audit, Finance |
| 6 | GRN Summary | `/api/v1/reports/grn-summary` | Store, Purchase |
| 7 | GRN vs Invoice | `/api/v1/reports/grn-vs-invoice` | Finance, Audit |
| 8 | Price Variance | `/api/v1/reports/price-variance` | Purchase, Finance |
| 9 | PO vs GRN | `/api/v1/reports/po-vs-grn` | Purchase |
| 10 | Inter-Branch Transfer | `/api/v1/reports/inter-branch-transfers` | Operations |
| 11 | Issue to Production | `/api/v1/reports/issue-to-production` | Production, Finance |
| 12 | Non-Moving Stock | `/api/v1/reports/non-moving-stock` | Operations, Finance |
| 13 | Supplier Performance | `/api/v1/reports/supplier-performance` | Purchase, Management |
| 14 | Audit Exceptions | `/api/v1/reports/audit-exceptions` | Audit, Internal Control |

---

## Common Query Parameters

All reports accept these common filters:

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `branchIds` | List<String> | No | Branch IDs (comma-separated). Omit for ALL branches. |
| `fromDate` | Date | No | Start date (YYYY-MM-DD), inclusive |
| `toDate` | Date | No | End date (YYYY-MM-DD), inclusive |
| `itemId` | String | No | Filter by specific item |
| `suppId` | String | No | Filter by specific supplier |

---

## Report 1: Current Stock Report

### Business Purpose
Shows the current quantity on hand at each branch and location. This is the primary report for daily inventory visibility.

### Who Uses It
- **Store Team:** Check stock availability
- **Purchase:** Identify reorder needs
- **Management:** Inventory oversight

### Endpoint
```
GET /api/v1/reports/current-stock
```

### Query Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `branchIds` | List<String> | Filter by branches |
| `itemId` | String | Filter by item |
| `locationId` | String | Filter by storage location |

### Response Fields
| Field | Description |
|-------|-------------|
| `branchId`, `branchName` | Branch details |
| `itemId`, `itemDesc` | Item details |
| `locationId`, `locationName` | Storage location |
| `qtyOnHand` | Current quantity available |
| `avgCost` | Average unit cost |
| `stockValue` | Total value (qty × avg cost) |

### Example Usage
```bash
# All branches, all items
GET /api/v1/reports/current-stock

# Single branch
GET /api/v1/reports/current-stock?branchIds=BLR01

# Multiple branches, specific item
GET /api/v1/reports/current-stock?branchIds=BLR01,DEL01&itemId=MLK001
```

### SQL Query (Optimized)
```sql
SELECT
    bms.branch_id, b.branch_name,
    bms.item_id, i.item_desc,
    bms.location_id, l.location_name,
    bms.qty_on_hand, bms.avg_cost,
    (bms.qty_on_hand * bms.avg_cost) as stock_value
FROM branch_material_stock bms
JOIN branch_master b ON bms.branch_id = b.branch_id
JOIN item_master i ON bms.item_id = i.item_id
JOIN location_master l ON bms.location_id = l.location_id
WHERE (:branchIds IS NULL OR bms.branch_id IN (:branchIds))
  AND (:itemId IS NULL OR bms.item_id = :itemId)
  AND bms.qty_on_hand > 0
ORDER BY bms.branch_id, i.item_desc;
```

### Indexes Used
- `branch_material_stock(branch_id)`
- `branch_material_stock(item_id)`
- `item_master(item_id)`

---

## Report 2: Consolidated Stock Summary

### Business Purpose
Shows total stock per item across the entire company (or selected branches) with a branch-wise breakup. Useful for company-wide inventory planning.

### Who Uses It
- **Management:** Company-wide inventory overview
- **Finance:** Total inventory valuation
- **Planning:** Distribution decisions

### Endpoint
```
GET /api/v1/reports/consolidated-stock
```

### Response Fields
| Field | Description |
|-------|-------------|
| `itemId`, `itemDesc` | Item details |
| `totalQty` | Total quantity across all branches |
| `totalValue` | Total inventory value |
| `branchBreakup[]` | Array of branch-wise quantities |
| └ `branchId`, `branchName` | Branch details |
| └ `qty`, `value` | Quantity and value at this branch |

### Example
```json
{
  "itemId": "MLK001",
  "itemDesc": "Full Cream Milk 500ml",
  "totalQty": 5000,
  "totalValue": 125000.00,
  "branchBreakup": [
    {"branchId": "BLR01", "branchName": "Bangalore", "qty": 2000, "value": 50000},
    {"branchId": "DEL01", "branchName": "Delhi", "qty": 3000, "value": 75000}
  ]
}
```

---

## Report 3: Stock Ledger Report

### Business Purpose
Complete audit trail of ALL stock movements. Every GRN, Issue, and Transfer is recorded here. This is the source of truth for reconciliation.

### Who Uses It
- **Auditors:** Complete movement history
- **Finance:** Stock reconciliation
- **Operations:** Investigate discrepancies

### Endpoint
```
GET /api/v1/reports/stock-ledger
```

### Query Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `branchIds` | List<String> | Filter by branches |
| `itemId` | String | Filter by item |
| `fromDate`, `toDate` | Date | Date range |
| `txnType` | String | GRN, ISSUE, TRANSFER_IN, TRANSFER_OUT |

### Response Fields
| Field | Description |
|-------|-------------|
| `ledgerId` | Unique entry ID |
| `txnDate` | Transaction date |
| `txnType` | GRN / ISSUE / TRANSFER_IN / TRANSFER_OUT |
| `refId` | Reference to GRN/Issue/Transfer ID |
| `qtyIn` | Quantity added |
| `qtyOut` | Quantity removed |
| `rate` | Unit price at this transaction |
| `balanceQty` | Running balance after this entry |

### Important
- Ledger is **append-only** - no updates, no deletes
- `balanceQty` is running total after each entry

---

## Report 4: Stock Aging Report

### Business Purpose
Shows how old the current stock is, based on GRN date. Aging buckets:
- **0-30 days:** Fresh stock
- **31-60 days:** Moderate
- **61-90 days:** Getting old
- **90+ days:** Stale/Dead stock

### Who Uses It
- **Operations:** Ensure FIFO compliance
- **Finance:** Provision for obsolete stock
- **Quality:** Identify expired risk

### Endpoint
```
GET /api/v1/reports/stock-aging
```

### Response Fields
| Field | Description |
|-------|-------------|
| `agingBucket` | 0-30, 31-60, 61-90, or 90+ |
| `qty` | Quantity in this age bucket |
| `rate` | GRN rate for this batch |
| `value` | Total value |
| `ageDays` | Exact age in days |

### Key Logic
Age is calculated from GRN date using `qty_remaining` from `grn_detail`.

---

## Report 5: FIFO Consumption Report

### Business Purpose
Shows exactly which GRN batches were consumed for each issue. Critical for:
- FIFO compliance verification
- Cost accuracy auditing
- Material traceability

### Who Uses It
- **Auditors:** Verify FIFO is followed
- **Finance:** Validate issue costs
- **Quality:** Trace material source

### Endpoint
```
GET /api/v1/reports/fifo-consumption
```

### Response Fields
| Field | Description |
|-------|-------------|
| `issueId` | Issue transaction ID |
| `itemId` | Item issued |
| `issuedQty` | Total quantity issued |
| `weightedAvgRate` | FIFO weighted average rate |
| `grnConsumptions[]` | Array of GRN batches consumed |
| └ `grnId`, `grnDate` | GRN details |
| └ `qtyConsumed`, `rate` | How much consumed at what rate |

### Example
```json
{
  "issueId": 8001,
  "itemId": "MLK001",
  "issuedQty": 200,
  "weightedAvgRate": 24.75,
  "grnConsumptions": [
    {"grnId": 4998, "grnDate": "2026-01-15", "qtyConsumed": 50, "rate": 24.00},
    {"grnId": 5001, "grnDate": "2026-02-01", "qtyConsumed": 150, "rate": 25.00}
  ]
}
```
**Calculation:** (50 × 24 + 150 × 25) / 200 = 24.75

---

## Report 6: GRN Summary Report

### Business Purpose
Overview of all goods received. Shows receipt status, values, and approvals.

### Who Uses It
- **Store:** Track deliveries
- **Purchase:** Monitor supplier deliveries
- **Finance:** Verify receipt totals

### Endpoint
```
GET /api/v1/reports/grn-summary
```

### Response Fields
| Field | Description |
|-------|-------------|
| `grnId` | GRN number |
| `branchId`, `branchName` | Receiving branch |
| `suppId`, `suppName` | Supplier |
| `invoiceNo` | Supplier invoice number |
| `grnDate` | Receipt date |
| `totalQty`, `totalValue` | Total received |
| `status` | DRAFT / PENDING_APPROVAL / POSTED |
| `approvedBy` | Who approved |

---

## Report 7: GRN vs Invoice Comparison

### Business Purpose
Three-way matching: Compare GRN amount vs Invoice amount to identify discrepancies.

### Who Uses It
- **Finance:** Payment verification
- **Audit:** Fraud detection
- **Purchase:** Supplier disputes

### Endpoint
```
GET /api/v1/reports/grn-vs-invoice
```

### Response Fields
| Field | Description |
|-------|-------------|
| `invoiceAmount` | Amount on supplier invoice |
| `grnAmount` | Calculated from GRN lines |
| `difference` | Invoice - GRN |
| `differencePercent` | Variance % |
| `approvalStatus` | GRN status |

---

## Report 8: Price Variance Report

### Business Purpose
Identify when GRN price differs from reference price. Used for:
- Cost control
- Negotiation analysis
- Approval tracking

### Who Uses It
- **Purchase:** Price negotiation data
- **Finance:** Cost variance analysis
- **Audit:** Approval verification

### Endpoint
```
GET /api/v1/reports/price-variance
```

### Query Parameters
| Parameter | Type | Description |
|-----------|------|-------------|
| `minVariance` | BigDecimal | Only show variances > this % |

### Response Fields
| Field | Description |
|-------|-------------|
| `lastReferencePrice` | Expected price (from item master) |
| `enteredPrice` | Actual GRN price |
| `varianceAmount` | Difference |
| `variancePercent` | Variance % |
| `approvedBy`, `approvalDate` | Approval details |

---

## Report 9: PO vs GRN Report

### Business Purpose
Track purchase order fulfillment. Shows ordered vs received quantities.

### Who Uses It
- **Purchase:** Follow up on pending deliveries
- **Store:** Anticipate arrivals
- **Finance:** Commitment tracking

### Endpoint
```
GET /api/v1/reports/po-vs-grn
```

### Response Fields
| Field | Description |
|-------|-------------|
| `qtyOrdered` | PO quantity |
| `qtyReceived` | Total received so far |
| `pendingQty` | Ordered - Received |
| `fulfillmentPercent` | % complete |
| `poStatus` | OPEN / PARTIAL / CLOSED |

---

## Report 10: Inter-Branch Transfer Report

### Business Purpose
Track stock movements between branches. Identify shortages (sent ≠ received).

### Who Uses It
- **Operations:** Logistics tracking
- **Finance:** Inter-branch reconciliation
- **Audit:** Shortage investigation

### Endpoint
```
GET /api/v1/reports/inter-branch-transfers
```

### Response Fields
| Field | Description |
|-------|-------------|
| `fromBranch`, `toBranch` | Source and destination |
| `qtySent` | Quantity dispatched |
| `qtyReceived` | Quantity received |
| `shortageQty` | Sent - Received (should be 0) |
| `status` | CREATED / IN_TRANSIT / RECEIVED |

---

## Report 11: Issue to Production Report

### Business Purpose
Track materials issued for production/consumption. Includes FIFO-calculated cost.

### Who Uses It
- **Production:** Consumption tracking
- **Finance:** Cost allocation
- **Operations:** Usage patterns

### Endpoint
```
GET /api/v1/reports/issue-to-production
```

### Response Fields
| Field | Description |
|-------|-------------|
| `qtyIssued` | Quantity issued |
| `fifoRate` | Weighted average FIFO rate |
| `totalValue` | Consumption value |
| `issuedTo` | Recipient (person/dept/purpose) |

---

## Report 12: Non-Moving / Slow-Moving Stock

### Business Purpose
Identify dead stock for clearance or write-off.
- **Non-Moving:** No movement in 90+ days
- **Slow-Moving:** No movement in 30-90 days

### Who Uses It
- **Operations:** Clearance planning
- **Finance:** Obsolescence provision
- **Management:** Working capital optimization

### Endpoint
```
GET /api/v1/reports/non-moving-stock
```

### Query Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `minDays` | Integer | 30 | Minimum days since last movement |

### Response Fields
| Field | Description |
|-------|-------------|
| `lastMovementDate` | Last GRN/Issue date |
| `daysSinceLastMovement` | Days since last movement |
| `movementCategory` | NON_MOVING or SLOW_MOVING |
| `value` | Stuck capital |

---

## Report 13: Supplier Performance Report

### Business Purpose
Evaluate supplier performance company-wide. Includes:
- Total GRNs
- Value supplied
- Price variance history
- Branch distribution

### Who Uses It
- **Purchase:** Vendor rating
- **Management:** Strategic sourcing
- **Finance:** Supplier risk assessment

### Endpoint
```
GET /api/v1/reports/supplier-performance
```

### Response Fields
| Field | Description |
|-------|-------------|
| `totalGrns` | Number of GRNs received |
| `totalValueSupplied` | Total purchase value |
| `avgPriceVariancePercent` | Average price variance |
| `grnWithVariance` | Count of GRNs with >5% variance |
| `branchDistribution[]` | Which branches buy from this supplier |

---

## Report 14: Audit & Exception Report

### Business Purpose
Consolidated view of all exceptions and anomalies:
- GRNs without PO (bypass procurement)
- High price variance approvals
- Transfer shortages

### Who Uses It
- **Internal Audit:** Exception review
- **Management:** Control effectiveness
- **Finance:** Risk assessment

### Endpoint
```
GET /api/v1/reports/audit-exceptions
```

### Query Parameters
| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `minVariance` | BigDecimal | 5 | Variance % threshold |

### Response Fields
| Field | Description |
|-------|-------------|
| `exceptionType` | GRN_WITHOUT_PO, HIGH_PRICE_VARIANCE, TRANSFER_SHORTAGE |
| `severity` | LOW, MEDIUM, HIGH, CRITICAL |
| `description` | Human-readable explanation |
| `amount` | Financial impact |
| `approvedBy`, `approvalDate` | Override details |

### Severity Rules
- **CRITICAL:** Price variance > 20%
- **HIGH:** Price variance > 10%, Transfer shortage > 10%
- **MEDIUM:** GRN without PO, Moderate variances

---

## Database Indexes Required

For optimal report performance, ensure these indexes exist:

```sql
-- Stock tables
CREATE INDEX idx_bms_branch ON branch_material_stock(branch_id);
CREATE INDEX idx_bms_item ON branch_material_stock(item_id);
CREATE INDEX idx_msl_branch_date ON material_stock_ledger(branch_id, txn_date);
CREATE INDEX idx_msl_item ON material_stock_ledger(item_id);

-- GRN tables
CREATE INDEX idx_grn_branch_date ON grn_header(branch_id, grn_date);
CREATE INDEX idx_grn_supp ON grn_header(supp_id);
CREATE INDEX idx_grnd_qty_remaining ON grn_detail(qty_remaining);

-- Issue tables
CREATE INDEX idx_issue_branch_date ON issue_header(branch_id, issue_date);
CREATE INDEX idx_fifo_issue ON issue_fifo_consumption(issue_id, item_id);

-- PO tables
CREATE INDEX idx_po_branch_status ON po_header(branch_id, status);

-- Transfer tables
CREATE INDEX idx_transfer_branches ON stock_transfer_header(from_branch, to_branch);
```

---

## API Error Handling

All report endpoints return standard error responses:

```json
{
  "timestamp": "2026-02-06T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format. Use YYYY-MM-DD",
  "path": "/api/v1/reports/current-stock"
}
```

---

## Performance Considerations

1. **Date Range:** Always provide `fromDate`/`toDate` for large datasets
2. **Branch Filter:** Use specific branches instead of ALL when possible
3. **Pagination:** Large reports may need pagination (future enhancement)
4. **Caching:** Consider caching consolidated reports

---

*Document generated for Material Management System v1.0*
