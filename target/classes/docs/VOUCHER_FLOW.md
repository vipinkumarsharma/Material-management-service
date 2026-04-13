# Voucher Flow — Deep-Dive Documentation

> **Scope:** Voucher Type Master · Voucher Series Master · VoucherNumberService ·
> Purchase Voucher · VoucherEditLog
>
> **Migrations:** V24 · V25 · V26 · V27
> **Base URL:** `http://<host>:8080/mms`

---

## Table of Contents

1. [Overview — Why This Exists](#1-overview--why-this-exists)
2. [Voucher Type Master](#2-voucher-type-master)
3. [Voucher Series Master](#3-voucher-series-master)
4. [Series Restart Schedule](#4-series-restart-schedule)
5. [VoucherNumberService — The Core Algorithm](#5-vouchernumberservice--the-core-algorithm)
6. [Integration With Transaction Services](#6-integration-with-transaction-services)
7. [Purchase Voucher — 3-Step Purchase Flow](#7-purchase-voucher--3-step-purchase-flow)
8. [VoucherEditLog — Immutable Audit Trail](#8-vouchereditlog--immutable-audit-trail)
9. [Database Schema Diagram](#9-database-schema-diagram)
10. [API Quick Reference](#10-api-quick-reference)
11. [Setup Checklist](#11-setup-checklist)

---

## 1. Overview — Why This Exists

### TallyPrime Analogy

TallyPrime organises every financial document through a three-level hierarchy:

```
Voucher Type  →  Voucher Series  →  Document Number
   (what)           (which set)        (the value)
```

| TallyPrime concept | MMS equivalent | Purpose |
|---|---|---|
| Voucher Type | `VoucherTypeMaster` | Defines behaviour rules (auto-number, approval, narration, etc.) |
| Voucher Series | `VoucherSeriesMaster` | Holds the counter + format for one branch |
| Voucher Number | Generated string | The actual numbered identifier on a document |

### How MMS Adapts It

MMS is multi-branch, so a series is scoped to **(voucherTypeId, branchId)**. Each branch
can have its own counter, prefix, and restart schedule. When a GRN is posted at branch
`BNG01` with voucher type `GRN-STD`, the system finds the default series for that
(type, branch) pair, locks it, increments the counter, and stamps the resulting string
onto the `grn_header.voucher_number` column.

### The 3-Step Purchase Flow

```
Purchase Order (PO)
        │
        ▼
Goods Receipt Note (GRN)   ← stock enters the system here
        │
        ▼
Purchase Voucher (PV)       ← financial recognition (accounts payable)
```

Before this feature, POs and GRNs had no voucher numbers and no financial voucher
document. The voucher flow adds:
- Automatic sequential numbering on GRNs, Issues, Transfers, Dept-Transfers
- A dedicated Purchase Voucher entity that carries the full financial breakdown
  (gross, discount, GST, cess, net, round-off)
- An immutable audit log that records every create / status change / approval

---

## 2. Voucher Type Master

**Entity:** `entity/master/VoucherTypeMaster.java`
**Table:** `voucher_type_master`
**Controller:** `controller/master/VoucherTypeController.java`

A Voucher Type is the *template* that controls how documents of that kind behave. You
create one per document category (e.g. "Standard GRN", "Interunit Transfer", "Purchase
Bill").

### Field Reference

| Field | DB column | Default | Effect |
|---|---|---|---|
| `voucherTypeId` | `voucher_type_id` PK | — | User-assigned ID (max 20 chars) |
| `voucherTypeName` | `voucher_type_name` | — | Human-readable label |
| `baseTxnType` | `base_txn_type` | — | Links to a core transaction kind: `GRN`, `ISSUE`, `TRANSFER`, `DEPT_TRANSFER`, `PURCHASE_VOUCHER` |
| `abbreviation` | `abbreviation` | — | Short code used in UI display (e.g. `GRN`) |
| `active` | `is_active` | `true` | Inactive types are excluded from lookups |
| `numberingMethod` | `numbering_method` | `AUTOMATIC` | See below |
| `numberingOnDeletion` | `numbering_on_deletion` | `RETAIN_ORIGINAL` | See below |
| `showUnusedNumbers` | `show_unused_numbers` | `false` | If true, gaps left by deletions are displayed |
| `preventDuplicates` | `prevent_duplicates` | `true` | Blocks saving the same voucher number twice |
| `allowZeroValued` | `allow_zero_valued` | `false` | If false, zero-value documents are rejected |
| `optionalDefault` | `is_optional_default` | `false` | Marks the document as informational-only by default |
| `useEffectiveDates` | `use_effective_dates` | `false` | Adds a separate `effective_date` field distinct from the transaction date |
| `effectiveDateLabel` | `effective_date_label` | `"Effective Date"` | UI label for the effective date field |
| `allowNarration` | `allow_narration` | `true` | If false, narration field is hidden entirely |
| `narrationMandatory` | `narration_mandatory` | `false` | Validation: saves fail without a narration |
| `narrationPerLine` | `narration_per_line` | `false` | When true, each line item can carry its own narration (`line_narration` column) |
| `printAfterSave` | `print_after_save` | `false` | Hint to UI to trigger print dialog after save |
| `requireApproval` | `require_approval` | `false` | Enables approval workflow for this type |
| `approvalAmountLimit` | `approval_amount_limit` | `null` | Net amount above this threshold requires approval |

#### `numberingMethod` values

| Value | Behaviour |
|---|---|
| `AUTOMATIC` | `VoucherNumberService` generates the next number from the series |
| `MANUAL` | User supplies the voucher number; system still checks `preventDuplicates` |
| `NONE` | No numbering; `voucher_number` is always null |

#### `numberingOnDeletion` values

| Value | Behaviour |
|---|---|
| `RETAIN_ORIGINAL` | The deleted number is gone forever (a gap appears in the sequence) |
| `RENUMBER` | On deletion the system could reuse the slot (implementation TBD; gap prevention) |

### API Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/master/voucher-types` | Create a new voucher type |
| `PUT` | `/api/v1/master/voucher-types/{id}` | Update an existing type |
| `GET` | `/api/v1/master/voucher-types` | List all types (filter: `?baseTxnType=GRN`) |
| `GET` | `/api/v1/master/voucher-types/{id}` | Get by ID |

### Sample JSON — Create Voucher Type

```json
POST /mms/api/v1/master/voucher-types
{
  "voucherTypeId": "GRN-STD",
  "voucherTypeName": "Standard Goods Receipt",
  "baseTxnType": "GRN",
  "abbreviation": "GRN",
  "numberingMethod": "AUTOMATIC",
  "useEffectiveDates": false,
  "allowNarration": true,
  "narrationMandatory": false,
  "narrationPerLine": false,
  "requireApproval": true,
  "approvalAmountLimit": 50000.00,
  "preventDuplicates": true
}
```

Response wraps the saved entity in `ApiResponse<VoucherTypeResponse>`.

---

## 3. Voucher Series Master

**Entity:** `entity/master/VoucherSeriesMaster.java`
**Table:** `voucher_series_master`
**Controller:** `controller/master/VoucherSeriesController.java`

A Series is the **counter** for one (voucherTypeId, branchId) combination. It holds
the current number, the format rules (width, prefix, suffix), and the restart policy.

### Key Design Rule

The DB enforces a unique constraint on `(voucher_type_id, branch_id, is_default)` with
`is_default = 1`. Only one series per (type, branch) pair can be the default. The system
always picks the default series when generating a number.

### Field Reference

| Field | Default | Purpose |
|---|---|---|
| `seriesId` | User-assigned | Primary key (max 20 chars) |
| `seriesName` | — | Human label, e.g. "Bangalore GRN 2024-25" |
| `voucherTypeId` | — | FK to `voucher_type_master` |
| `branchId` | — | FK to `branch_master` |
| `startingNumber` | `1` | Reset target when a restart occurs |
| `currentNumber` | `1` | The next number that will be issued |
| `numberWidth` | `6` | Zero-pad width (only used when `prefillWithZero = true`) |
| `prefillWithZero` | `true` | Pads with leading zeros: `1` → `000001` |
| `prefixDetails` | `""` | String prepended to the number, e.g. `"BNG/GRN/"` |
| `suffixDetails` | `""` | String appended, e.g. `"/25"` |
| `restartPeriodicity` | `ANNUALLY` | When the counter resets automatically |
| `lastResetDate` | `null` | Date of the last counter reset |
| `nextRestartDate` | `null` | Informational: computed next reset |
| `defaultSeries` | `false` | Must be `true` for auto-generation to pick this series |
| `active` | `true` | Inactive series are skipped |

### `restartPeriodicity` values

| Value | When the counter resets |
|---|---|
| `NONE` | Never — counter increments forever |
| `MONTHLY` | On first use in a new calendar month |
| `QUARTERLY` | On first use in a new quarter (Jan/Apr/Jul/Oct) |
| `ANNUALLY` | On first use in a new calendar year |
| `CUSTOM` | Only via explicit `VoucherSeriesRestartSchedule` entries (see §4) |

### How a Number Looks

```
prefixDetails + zeroPad(currentNumber, numberWidth) + suffixDetails

Example with prefixDetails="BNG/GRN/", numberWidth=5, currentNumber=42, suffixDetails="/25":
  → "BNG/GRN/00042/25"
```

### API Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/master/voucher-series` | Create a series |
| `PUT` | `/api/v1/master/voucher-series/{id}` | Update a series |
| `GET` | `/api/v1/master/voucher-series` | List (filter: `?voucherTypeId=&branchId=`) |
| `GET` | `/api/v1/master/voucher-series/{id}` | Get by ID |
| `GET` | `/api/v1/master/voucher-series/{id}/preview-next` | Preview next number (no increment) |
| `POST` | `/api/v1/master/voucher-series/{id}/restart-schedule` | Add a custom restart date |
| `GET` | `/api/v1/master/voucher-series/{id}/restart-schedule` | List restart schedules |

### Sample JSON — Create Series

```json
POST /mms/api/v1/master/voucher-series
{
  "seriesId": "BNG-GRN-25",
  "seriesName": "Bangalore GRN 2024-25",
  "voucherTypeId": "GRN-STD",
  "branchId": "BNG01",
  "startingNumber": 1,
  "numberWidth": 5,
  "prefillWithZero": true,
  "prefixDetails": "BNG/GRN/",
  "suffixDetails": "/25",
  "restartPeriodicity": "ANNUALLY",
  "defaultSeries": true
}
```

---

## 4. Series Restart Schedule

**Entity:** `entity/master/VoucherSeriesRestartSchedule.java`
**Table:** `voucher_series_restart_schedule`

### Why You'd Use It

- Indian financial year change (April 1) when calendar year doesn't match
- Mid-year prefix change (e.g. rebranding branch codes)
- One-off reset after a numbering error

The restart schedule lets you pre-schedule a counter reset at a specific future date,
with optional prefix/suffix overrides. On the first voucher generated **on or after**
`applicableFromDate`, the system applies the schedule — resetting the counter and
updating prefix/suffix if overrides are provided.

### Fields

| Field | Purpose |
|---|---|
| `restartId` | Auto-generated PK (Long) |
| `seriesId` | FK to `voucher_series_master` |
| `applicableFromDate` | Date on/after which the restart fires (one-time) |
| `startingNumber` | Counter resets to this value (default: 1) |
| `prefixOverride` | Replaces `prefixDetails` on the series (null = keep existing) |
| `suffixOverride` | Replaces `suffixDetails` (null = keep existing) |

### Priority: Custom Schedule Beats Periodicity

When `VoucherNumberService` runs:
1. It first checks if there is an applicable restart schedule (see §5 for detail).
2. If a schedule fires → restart is applied, periodicity check is **skipped**.
3. Only if no custom schedule applies does the periodicity logic run.

### API

```json
POST /mms/api/v1/master/voucher-series/{id}/restart-schedule
{
  "applicableFromDate": "2025-04-01",
  "startingNumber": 1,
  "prefixOverride": "BNG/GRN/FY26/",
  "suffixOverride": null
}
```

Preview the next number after potential restarts (read-only):
```
GET /mms/api/v1/master/voucher-series/{id}/preview-next
```

---

## 5. VoucherNumberService — The Core Algorithm

**Service:** `service/master/VoucherNumberService.java`

### `generateVoucherNumber(voucherTypeId, branchId)`

This is the entry point called by GrnService, IssueService, etc.

#### Step-by-Step Flow

```
1. Find the default active series for (voucherTypeId, branchId)
   → SQL: SELECT ... WHERE voucher_type_id=? AND branch_id=? AND is_default=1 AND is_active=1
   → Lock: PESSIMISTIC_WRITE (SELECT ... FOR UPDATE)
   → If not found → throw MmsException (caller catches and returns null number gracefully)

2. Get today's date in Asia/Kolkata timezone

3. Check custom restart schedule
   → Query: SELECT * FROM voucher_series_restart_schedule
            WHERE series_id=? AND applicable_from_date <= today
            ORDER BY applicable_from_date DESC
   → Take the latest (index 0)
   → Apply if: lastResetDate IS NULL  OR  lastResetDate < schedule.applicableFromDate
     • series.currentNumber  ← schedule.startingNumber
     • series.lastResetDate  ← today
     • if prefixOverride != null → series.prefixDetails ← prefixOverride
     • if suffixOverride != null → series.suffixDetails ← suffixOverride
   → restarted = true; skip step 4

4. If no custom restart applied — check periodicity
   MONTHLY  → reset if lastResetDate is null or in a different (year, month)
   QUARTERLY → reset if in a different quarter (Jan-Mar / Apr-Jun / Jul-Sep / Oct-Dec)
   ANNUALLY  → reset if in a different calendar year
   NONE      → do nothing
   (CUSTOM   → handled only via schedule in step 3; nothing here)

   On reset:
     • series.currentNumber ← series.startingNumber
     • series.lastResetDate ← today

5. Format the number
   if prefillWithZero:
     numStr = String.format("%0{numberWidth}d", currentNumber)   e.g. "000042"
   else:
     numStr = String.valueOf(currentNumber)                       e.g. "42"
   result = prefixDetails + numStr + suffixDetails

6. Increment: series.currentNumber += 1
   Save series row (lock released on transaction commit)

7. Return the formatted string
```

#### Why PESSIMISTIC_WRITE?

Without a row-level lock, two concurrent requests for the same series could both read
`currentNumber = 42`, both format `000042`, and both increment to `43` — producing a
duplicate voucher number. `SELECT ... FOR UPDATE` ensures only one transaction can hold
the counter at a time; the second waits until the first commits.

#### Restart Decision Tree (Summary)

```
generateVoucherNumber()
         │
         ▼
  Find default series (locked)
         │
         ▼
  Custom schedule applicable?
    YES ──────────────────────────► Apply schedule restart
         │                              (reset counter, update prefix/suffix)
         NO                             │
         │                             │
         ▼                             │
  Periodicity restart needed?          │
    YES ──────────────────────────► Apply periodicity restart
         │                              (reset counter to startingNumber)
         NO                             │
         │                             │
         ▼◄────────────────────────────┘
  Format number
         │
         ▼
  Increment & save
         │
         ▼
  Return string
```

### `previewNextNumber(seriesId)`

```java
@Transactional(readOnly = true)
public String previewNextNumber(String seriesId)
```

- Fetches the series by ID (no lock).
- Calls `formatNumber()` with the **current** state — it does **not** simulate restarts.
- Returns what the next generated number *would look like* absent any pending restart.
- Does **not** increment the counter.

### Graceful No-Series Fallback

All callers wrap `generateVoucherNumber` in a try/catch:

```java
private String generateVoucherNumber(String voucherTypeId, String branchId) {
    if (voucherTypeId == null || branchId == null) return null;
    try {
        return voucherNumberService.generateVoucherNumber(voucherTypeId, branchId);
    } catch (MmsException e) {
        log.debug("No series configured — voucher number not generated");
        return null;
    }
}
```

When `voucher_number` is null the document is saved normally; no numbering occurs.

---

## 6. Integration With Transaction Services

### Which Services Use Voucher Numbering

| Service | Entity type string | voucherTypeId source |
|---|---|---|
| `GrnService` | `"GRN"` | `GrnCreateRequest.voucherTypeId` |
| `IssueService` | `"ISSUE"` | `IssueCreateRequest.voucherTypeId` |
| `TransferService` | `"TRANSFER"` | `TransferCreateRequest.voucherTypeId` |
| `DeptTransferService` | `"DEPT_TRANSFER"` | `DeptTransferCreateRequest.voucherTypeId` |

### Modified Fields on Transaction Headers

All five header tables (`grn_header`, `issue_header`, `po_header`, `dept_transfer_header`,
`stock_transfer_header`) received three new columns via **V25**:

| Column | Type | Purpose |
|---|---|---|
| `voucher_number` | `VARCHAR(50)` | The generated document number |
| `voucher_type_id` | `VARCHAR(20)` | FK to `voucher_type_master` |
| `effective_date` | `DATE` | Separate effective date (only relevant when `useEffectiveDates=true`) |

### How `effective_date` Differs From Transaction Date

- `grn_date` (or `issue_date` etc.) = the physical date the goods moved.
- `effective_date` = the accounting date for ledger recognition (useful when goods
  arrive on Mar 31 but should post to the next financial year on Apr 1).
- The field is only presented to the user when the linked `VoucherTypeMaster` has
  `useEffectiveDates = true`. If the type requires it and the request omits it,
  `PurchaseVoucherService` throws an `MmsException`.

### Code Pattern (from GrnService)

```java
// In GrnService.createGrn():

// 1. Generate number (graceful fallback to null if no series)
String voucherNumber = generateVoucherNumber(request.getVoucherTypeId(), request.getBranchId());

// 2. Stamp it onto the header
GrnHeader grn = GrnHeader.builder()
        ...
        .voucherNumber(voucherNumber)
        .voucherTypeId(request.getVoucherTypeId())
        .build();

grn = grnHeaderRepository.save(grn);

// 3. Log CREATE event in a separate transaction
editLogService.logCreate("GRN", grn.getGrnId(), grn.getVoucherNumber(),
        grn.getVoucherTypeId(), grn.getCreatedBy(),
        "GRN created for branch=" + grn.getBranchId() + ", supplier=" + grn.getSuppId());
```

---

## 7. Purchase Voucher — 3-Step Purchase Flow

**Entity:** `entity/purchase/PurchaseVoucherHeader.java`, `PurchaseVoucherDetail.java`
**Service:** `service/purchase/PurchaseVoucherService.java`
**Controller:** `controller/PurchaseVoucherController.java` → `/api/v1/purchase-voucher`

### Flow Diagram

```
┌──────────────────────────────────────────────────────┐
│ Step 1 — Purchase Order (optional)                   │
│ POST /api/v1/master/... (existing PO flow)           │
│ Status: DRAFT → APPROVED                             │
└──────────────────────┬───────────────────────────────┘
                       │ po_id linked (optional)
                       ▼
┌──────────────────────────────────────────────────────┐
│ Step 2 — Goods Receipt Note                          │
│ POST /api/v1/grn                                     │
│ Status: DRAFT → PENDING_APPROVAL → POSTED            │
│ Effect: stock enters system (ledger entry created)   │
│ Fields added: voucher_number, voucher_type_id,       │
│              effective_date                           │
└──────────────────────┬───────────────────────────────┘
                       │ grn_id linked
                       ▼
┌──────────────────────────────────────────────────────┐
│ Step 3 — Purchase Voucher                            │
│ POST /api/v1/purchase-voucher/from-grn/{grnId}       │
│ Status: DRAFT → PENDING_APPROVAL → POSTED            │
│ Effect: accounts payable recognised                  │
│ Fields: gross, discount, GST, cess, net, round-off   │
└──────────────────────────────────────────────────────┘
```

### Status Machine

```
DRAFT ──► PENDING_APPROVAL ──► POSTED
  │              │
  └──────────────┘  (submitForApproval)
                 │
                 └─────────────────────► POSTED  (approveAndPost)
```

Transitions are validated strictly:
- `submitForApproval` requires status = `DRAFT`
- `approveAndPost` requires status = `PENDING_APPROVAL`

### `createFromGrn` — What Gets Copied

When you call `POST /api/v1/purchase-voucher/from-grn/{grnId}`:

1. GRN must be in `POSTED` status (stock already received).
2. `existsByGrnId(grnId)` — only one PV per GRN is allowed.
3. `voucherTypeId` is resolved (from request or auto-detected from active `PURCHASE_VOUCHER` type).
4. `effective_date` validated if `useEffectiveDates = true`.
5. Header is built — `branchId`, `suppId`, `pvDate`, `poId`, `invoiceId` all copied from GRN.
6. All `GrnDetail` rows are copied into `PurchaseVoucherDetail`:
   - `qty` ← `qtyReceived`
   - `rate`, `grossAmount`, `discountPerc/Amount`, `gstPerc/Amount`, `cessPerc/Amount`, `netAmount` copied verbatim
7. Financial totals on the header (`grossAmount`, `discountAmount`, `gstAmount`,
   `cessAmount`, `netAmount`, `roundOffAmount`) come from the request body — the caller
   provides them from the supplier invoice.

The request body is optional for `from-grn` (defaults to empty object); only the
financial overrides and narration need to be provided.

### `createManual` — Standalone PV Without GRN

```
POST /api/v1/purchase-voucher
```

Used when a Purchase Voucher is raised independently (e.g., service purchase).
Line items are supplied in `request.details`. There is no GRN link.

### Financial Fields

| Field | Meaning |
|---|---|
| `grossAmount` | Sum of all line `qty × rate` |
| `discountAmount` | Total trade/cash discount |
| `gstAmount` | Total GST (CGST + SGST or IGST) |
| `cessAmount` | Additional cess if applicable |
| `netAmount` | `grossAmount − discountAmount + gstAmount + cessAmount` |
| `roundOffAmount` | ±adjustment to reach a round figure |

### Line Narration

When the linked `VoucherTypeMaster.narrationPerLine = true`, each `PurchaseVoucherDetail`
row has a `line_narration TEXT` column. Populate it via `PurchaseVoucherDetailRequest.lineNarration`.

### API Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/api/v1/purchase-voucher` | Create manually |
| `POST` | `/api/v1/purchase-voucher/from-grn/{grnId}` | Create from a posted GRN |
| `GET` | `/api/v1/purchase-voucher/{pvId}` | Get by ID |
| `GET` | `/api/v1/purchase-voucher` | List (filter: `?branchId=&status=`) |
| `POST` | `/api/v1/purchase-voucher/{pvId}/submit` | DRAFT → PENDING_APPROVAL |
| `POST` | `/api/v1/purchase-voucher/{pvId}/approve` | PENDING_APPROVAL → POSTED |

### Sample JSON — Full Flow

**Step 1: Create GRN with voucher type**
```json
POST /mms/api/v1/grn
{
  "branchId": "BNG01",
  "suppId": "SUPP001",
  "voucherTypeId": "GRN-STD",
  "grnDate": "2025-03-15",
  "details": [
    {
      "itemId": "ITEM001",
      "unitId": "KG",
      "locationId": "BNG-WH-A1",
      "qtyOrdered": 100,
      "qtyReceived": 100,
      "rate": 45.50,
      "discountPerc": 2,
      "gstPerc": 18
    }
  ]
}
```
Response includes `voucherNumber: "BNG/GRN/000001/25"`.

**Step 2: Submit GRN for approval**
```
POST /mms/api/v1/grn/{grnId}/submit?submittedBy=user123
```

**Step 3: Post GRN (approve)**
```
POST /mms/api/v1/grn/{grnId}/approve?approvedBy=manager01
```

**Step 4: Create Purchase Voucher from posted GRN**
```json
POST /mms/api/v1/purchase-voucher/from-grn/1001
{
  "voucherTypeId": "PV-STD",
  "pvDate": "2025-03-15",
  "supplierInvoiceNo": "INV-2025-887",
  "supplierInvoiceDate": "2025-03-14",
  "grossAmount": 4550.00,
  "discountAmount": 91.00,
  "gstAmount": 800.82,
  "cessAmount": 0,
  "netAmount": 5259.82,
  "roundOffAmount": 0.18,
  "narration": "Purchase from SUPP001 against PO#501",
  "createdBy": "user123"
}
```

**Step 5: Submit & Approve**
```
POST /mms/api/v1/purchase-voucher/2001/submit?submittedBy=user123
POST /mms/api/v1/purchase-voucher/2001/approve?approvedBy=manager01
```

---

## 8. VoucherEditLog — Immutable Audit Trail

**Entity:** `entity/audit/VoucherEditLog.java`
**Table:** `voucher_edit_log`
**Service:** `service/audit/VoucherEditLogService.java`
**Controller:** `controller/AuditController.java`

### What Gets Logged and When

| `changeType` | Triggered by | Key fields populated |
|---|---|---|
| `CREATE` | Document saved for the first time | `newValue` = human-readable summary |
| `STATUS_CHANGE` | Any status transition | `fieldName="status"`, `oldValue`, `newValue` |
| `APPROVE` | Approval action | `changedBy` = approver |
| `UPDATE` | Field-level edits | `fieldName`, `oldValue`, `newValue` |

Every call includes `entityType` (e.g. `"GRN"`, `"PURCHASE_VOUCHER"`) and `entityId`
(the numeric PK), so logs can be queried per document.

### `REQUIRES_NEW` Propagation — Why It Matters

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void logCreate(String entityType, Long entityId, ...) { ... }
```

`REQUIRES_NEW` **suspends** the caller's transaction and opens a fresh one for the log
write. This means:
- The audit entry is committed immediately.
- Even if the main transaction rolls back (e.g. due to a validation error), the log
  entry survives.
- This gives a true audit trail: you can see that an attempt was made even if the
  document was never fully saved.

### `FieldChange` Record

To stay under Checkstyle's `ParameterNumber` limit (max 7), field-update logging uses
a compact record:

```java
public record FieldChange(String fieldName, String oldValue, String newValue) {}

// Usage:
editLogService.logFieldUpdate(
    "GRN", grnId, voucher, typeId,
    new FieldChange("suppId", "OLD_SUPP", "NEW_SUPP"),
    changedBy
);
```

### API

```
GET /mms/api/v1/audit/{entityType}/{entityId}
```

`entityType` is case-insensitive (converted to uppercase in the controller).

### Sample Audit Log Response

```json
{
  "success": true,
  "data": [
    {
      "logId": 1,
      "entityType": "GRN",
      "entityId": 1001,
      "voucherNumber": "BNG/GRN/000001/25",
      "voucherTypeId": "GRN-STD",
      "changeType": "CREATE",
      "fieldName": null,
      "oldValue": null,
      "newValue": "GRN created for branch=BNG01, supplier=SUPP001",
      "changedBy": "user123",
      "changedAt": "2025-03-15T10:30:00",
      "remarks": null
    },
    {
      "logId": 2,
      "entityType": "GRN",
      "entityId": 1001,
      "voucherNumber": "BNG/GRN/000001/25",
      "voucherTypeId": "GRN-STD",
      "changeType": "STATUS_CHANGE",
      "fieldName": "status",
      "oldValue": "DRAFT",
      "newValue": "PENDING_APPROVAL",
      "changedBy": "user123",
      "changedAt": "2025-03-15T10:35:00",
      "remarks": null
    },
    {
      "logId": 3,
      "entityType": "GRN",
      "entityId": 1001,
      "voucherNumber": "BNG/GRN/000001/25",
      "voucherTypeId": "GRN-STD",
      "changeType": "APPROVE",
      "fieldName": null,
      "oldValue": null,
      "newValue": null,
      "changedBy": "manager01",
      "changedAt": "2025-03-15T11:00:00",
      "remarks": null
    },
    {
      "logId": 4,
      "entityType": "GRN",
      "entityId": 1001,
      "voucherNumber": "BNG/GRN/000001/25",
      "voucherTypeId": "GRN-STD",
      "changeType": "STATUS_CHANGE",
      "fieldName": "status",
      "oldValue": "PENDING_APPROVAL",
      "newValue": "POSTED",
      "changedBy": "manager01",
      "changedAt": "2025-03-15T11:00:00",
      "remarks": null
    }
  ]
}
```

---

## 9. Database Schema Diagram

```
voucher_type_master (PK: voucher_type_id)
  │
  ├──► voucher_series_master (PK: series_id, FK: voucher_type_id, branch_id)
  │          │
  │          └──► voucher_series_restart_schedule (PK: restart_id, FK: series_id)
  │
  ├──► purchase_voucher_header (PK: pv_id, FK: voucher_type_id, branch_id, supp_id)
  │          │
  │          └──► purchase_voucher_detail (PK: pv_id+item_id, FK: pv_id, item_id, unit_id, location_id)
  │
  ├──► grn_header.voucher_type_id  (FK, nullable)
  ├──► issue_header.voucher_type_id
  ├──► po_header.voucher_type_id
  ├──► dept_transfer_header.voucher_type_id
  └──► stock_transfer_header.voucher_type_id

voucher_edit_log (PK: log_id)
  — references entity by (entity_type VARCHAR, entity_id BIGINT) — no FK constraint
  — append-only; no updates or deletes
```

### Key Constraints

| Table | Constraint | Purpose |
|---|---|---|
| `voucher_series_master` | `UNIQUE(voucher_type_id, branch_id, is_default)` | Enforces one default per (type, branch) |
| `voucher_series_restart_schedule` | `INDEX(series_id, applicable_from_date)` | Fast lookup of applicable schedules |
| `purchase_voucher_detail` | `PRIMARY KEY(pv_id, item_id)` | One line per item per voucher |
| `purchase_voucher_header` | `INDEX(grn_id)` | Fast `existsByGrnId` check |

### Flyway Migrations

| Version | File | What it does |
|---|---|---|
| V24 | `V24__create_voucher_type_and_series.sql` | Creates `voucher_type_master`, `voucher_series_master`, `voucher_series_restart_schedule` |
| V25 | `V25__add_voucher_fields_to_transactions.sql` | Adds `voucher_number`, `voucher_type_id`, `effective_date` to 5 transaction tables |
| V26 | `V26__create_purchase_voucher_tables.sql` | Creates `purchase_voucher_header` and `purchase_voucher_detail` |
| V27 | `V27__create_voucher_edit_log.sql` | Creates `voucher_edit_log` |

---

## 10. API Quick Reference

All paths are relative to `/mms`. Responses wrapped in `ApiResponse<T>`.

### Voucher Type Master

| Method | Path | Purpose | Key Params |
|---|---|---|---|
| `POST` | `/api/v1/master/voucher-types` | Create voucher type | Body: VoucherTypeRequest |
| `PUT` | `/api/v1/master/voucher-types/{id}` | Update voucher type | Path: id |
| `GET` | `/api/v1/master/voucher-types` | List types | Query: `?baseTxnType=` |
| `GET` | `/api/v1/master/voucher-types/{id}` | Get by ID | Path: id |

### Voucher Series Master

| Method | Path | Purpose | Key Params |
|---|---|---|---|
| `POST` | `/api/v1/master/voucher-series` | Create series | Body: VoucherSeriesRequest |
| `PUT` | `/api/v1/master/voucher-series/{id}` | Update series | Path: id |
| `GET` | `/api/v1/master/voucher-series` | List series | Query: `?voucherTypeId=&branchId=` |
| `GET` | `/api/v1/master/voucher-series/{id}` | Get by ID | Path: id |
| `GET` | `/api/v1/master/voucher-series/{id}/preview-next` | Preview next number | Path: id |
| `POST` | `/api/v1/master/voucher-series/{id}/restart-schedule` | Add restart schedule | Body: applicableFromDate, startingNumber, prefixOverride, suffixOverride |
| `GET` | `/api/v1/master/voucher-series/{id}/restart-schedule` | List restart schedules | Path: id |

### Purchase Voucher

| Method | Path | Purpose | Key Params |
|---|---|---|---|
| `POST` | `/api/v1/purchase-voucher` | Create manually | Body: PurchaseVoucherCreateRequest |
| `POST` | `/api/v1/purchase-voucher/from-grn/{grnId}` | Create from posted GRN | Path: grnId, Body: optional overrides |
| `GET` | `/api/v1/purchase-voucher/{pvId}` | Get by ID | Path: pvId |
| `GET` | `/api/v1/purchase-voucher` | List | Query: `?branchId=&status=` |
| `POST` | `/api/v1/purchase-voucher/{pvId}/submit` | DRAFT → PENDING_APPROVAL | Query: `?submittedBy=` |
| `POST` | `/api/v1/purchase-voucher/{pvId}/approve` | PENDING_APPROVAL → POSTED | Query: `?approvedBy=` |

### Audit Log

| Method | Path | Purpose | Key Params |
|---|---|---|---|
| `GET` | `/api/v1/audit/{entityType}/{entityId}` | Get audit trail | Path: entityType (GRN/ISSUE/PURCHASE_VOUCHER/etc.), entityId |

---

## 11. Setup Checklist (End-to-End)

Follow these steps in order for a working voucher flow at a new branch.

### Step 1 — Create Voucher Type

```json
POST /mms/api/v1/master/voucher-types
{
  "voucherTypeId": "GRN-STD",
  "voucherTypeName": "Standard GRN",
  "baseTxnType": "GRN",
  "numberingMethod": "AUTOMATIC",
  "requireApproval": true,
  "approvalAmountLimit": 50000
}
```

Repeat for `baseTxnType` values: `ISSUE`, `DEPT_TRANSFER`, `TRANSFER`, `PURCHASE_VOUCHER`.

### Step 2 — Create Voucher Series for Each Branch

```json
POST /mms/api/v1/master/voucher-series
{
  "seriesId": "BNG-GRN-25",
  "seriesName": "Bangalore GRN FY25",
  "voucherTypeId": "GRN-STD",
  "branchId": "BNG01",
  "prefixDetails": "BNG/GRN/",
  "numberWidth": 5,
  "prefillWithZero": true,
  "restartPeriodicity": "ANNUALLY",
  "defaultSeries": true
}
```

Create one series per (voucherType, branch) combination. Set `defaultSeries: true`.

### Step 3 — (Optional) Add Restart Schedule

For Indian financial year (April 1 reset):
```json
POST /mms/api/v1/master/voucher-series/BNG-GRN-25/restart-schedule
{
  "applicableFromDate": "2025-04-01",
  "startingNumber": 1,
  "prefixOverride": "BNG/GRN/FY26/"
}
```

### Step 4 — Verify Preview

```
GET /mms/api/v1/master/voucher-series/BNG-GRN-25/preview-next
```

Expected response: `"BNG/GRN/00001"` (or whatever the current state yields).

### Step 5 — Create GRN With voucherTypeId

```json
POST /mms/api/v1/grn
{
  "branchId": "BNG01",
  "suppId": "SUPP001",
  "voucherTypeId": "GRN-STD",
  ...
}
```

The response will include `voucherNumber: "BNG/GRN/00001"`.

### Step 6 — Create Purchase Voucher From GRN

After GRN is POSTED:
```json
POST /mms/api/v1/purchase-voucher/from-grn/{grnId}
{ "voucherTypeId": "PV-STD", ... }
```

### Step 7 — Submit and Approve Purchase Voucher

```
POST /mms/api/v1/purchase-voucher/{pvId}/submit?submittedBy=user123
POST /mms/api/v1/purchase-voucher/{pvId}/approve?approvedBy=manager01
```

### Step 8 — Query Audit Log

```
GET /mms/api/v1/audit/PURCHASE_VOUCHER/{pvId}
GET /mms/api/v1/audit/GRN/{grnId}
```

---

## Key File Reference

| Component | File path |
|---|---|
| VoucherTypeMaster entity | `entity/master/VoucherTypeMaster.java` |
| VoucherSeriesMaster entity | `entity/master/VoucherSeriesMaster.java` |
| VoucherSeriesRestartSchedule entity | `entity/master/VoucherSeriesRestartSchedule.java` |
| VoucherNumberService | `service/master/VoucherNumberService.java` |
| VoucherTypeService | `service/master/VoucherTypeService.java` |
| VoucherSeriesService | `service/master/VoucherSeriesService.java` |
| VoucherTypeController | `controller/master/VoucherTypeController.java` |
| VoucherSeriesController | `controller/master/VoucherSeriesController.java` |
| PurchaseVoucherHeader entity | `entity/purchase/PurchaseVoucherHeader.java` |
| PurchaseVoucherDetail entity | `entity/purchase/PurchaseVoucherDetail.java` |
| PurchaseVoucherService | `service/purchase/PurchaseVoucherService.java` |
| PurchaseVoucherController | `controller/PurchaseVoucherController.java` |
| VoucherEditLog entity | `entity/audit/VoucherEditLog.java` |
| VoucherEditLogService | `service/audit/VoucherEditLogService.java` |
| AuditController | `controller/AuditController.java` |
| V24 migration | `db/migration/V24__create_voucher_type_and_series.sql` |
| V25 migration | `db/migration/V25__add_voucher_fields_to_transactions.sql` |
| V26 migration | `db/migration/V26__create_purchase_voucher_tables.sql` |
| V27 migration | `db/migration/V27__create_voucher_edit_log.sql` |
