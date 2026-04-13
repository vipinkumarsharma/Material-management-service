# Material Management System - Data Model Documentation

> **Version:** 1.0
> **Last Updated:** February 2026
> **Audience:** Developers, Operations Team, Auditors, Product Owners

---

## Table of Contents

1. [Master Data](#1-master-data)
   - [Branch Master](#11-branch-master)
   - [Group Master](#12-group-master)
   - [Sub Group Master](#13-sub-group-master)
   - [Unit Master](#14-unit-master)
   - [Manufacturer Master](#15-manufacturer-master)
   - [Supplier Master](#16-supplier-master)
   - [Item Master](#17-item-master)
   - [Location Master](#18-location-master)
2. [Purchase](#2-purchase)
   - [PO Header](#21-po-header)
   - [PO Detail](#22-po-detail)
   - [Supplier Invoice](#23-supplier-invoice)
3. [Inward](#3-inward)
   - [GRN Header](#31-grn-header)
   - [GRN Detail](#32-grn-detail)
4. [Outward](#4-outward)
   - [Issue Header](#41-issue-header)
   - [Issue Detail](#42-issue-detail)
   - [Issue FIFO Consumption](#43-issue-fifo-consumption)
5. [Transfer](#5-transfer)
   - [Stock Transfer Header](#51-stock-transfer-header)
   - [Stock Transfer Detail](#52-stock-transfer-detail)
6. [Stock](#6-stock)
   - [Material Stock Ledger](#61-material-stock-ledger)
   - [Branch Material Stock](#62-branch-material-stock)
7. [Governance](#7-governance)
   - [Role Master](#71-role-master)
   - [User Role Map](#72-user-role-map)
   - [Approval Rule](#73-approval-rule)

---

## 1. Master Data

Master data tables contain foundational reference data that rarely changes. They define the "what" and "where" of the system.

---

### 1.1 Branch Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `BranchMaster` |
| **Table Name** | `branch_master` |
| **Primary Key** | `branch_id` (VARCHAR 20) |

#### Business Name
**Branch / Warehouse / Store Location**

#### Purpose
Represents a physical location (warehouse, store, or distribution center) where materials are stored and managed. Each branch maintains its own stock independently.

#### When is it created?
- When a new warehouse or store is opened
- During initial system setup
- When expanding to a new geographic location

#### Who uses it?
| Team | Usage |
|------|-------|
| Operations | Identify which warehouse to send/receive materials |
| Finance | Track inventory value per location |
| Purchase | Know which branch needs stock replenishment |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Branch Code | `branch_id` | Unique identifier (e.g., "BLR01", "DEL02") |
| Branch Name | `branch_name` | Human-readable name (e.g., "Bangalore Main Warehouse") |
| GST Number | `gst_no` | Tax registration number for compliance |
| Address | `address_1` | Physical location for deliveries |
| Pincode | `pincode` | Postal code for logistics planning |

#### Stock Impact
**No direct stock impact** - This is reference data. Stock is tracked against branches in other tables.

#### Accounting Relevance
- **Tax Compliance:** GST number required for invoicing
- **Inventory Valuation:** Stock value calculated per branch
- **Audit:** Physical verification done branch-wise

#### Example
> **Scenario:** Country Delight opens a new distribution center in Noida.
> **Action:** Admin creates a new branch record:
> - Branch ID: `NOI01`
> - Branch Name: `Noida Distribution Center`
> - GST No: `09AAACN1234X1ZC`
> - Address: `Plot 45, Sector 62, Noida`
> - Pincode: `201301`

---

### 1.2 Group Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `GroupMaster` |
| **Table Name** | `group_master` |
| **Primary Key** | `group_id` (VARCHAR 20) |

#### Business Name
**Material Category / Item Group**

#### Purpose
Top-level classification of materials. Groups help organize inventory into broad categories for reporting, analysis, and management.

#### When is it created?
- During initial system setup
- When introducing an entirely new product category

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Filter items by category when ordering |
| Operations | View stock by category |
| Finance | Category-wise inventory reports |
| Management | Strategic decisions on category performance |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Group Code | `group_id` | Unique category code (e.g., "DAIRY", "PACK") |
| Group Name | `group_desc` | Category description (e.g., "Dairy Products") |

#### Stock Impact
**No stock impact** - Classification only.

#### Accounting Relevance
- **Reporting:** Category-wise inventory valuation
- **Analysis:** Profitability by product category

#### Example
> **Scenario:** Setting up initial categories.
> **Groups created:**
> - `DAIRY` - Dairy Products (Milk, Curd, Butter)
> - `PACK` - Packaging Materials (Bottles, Caps, Labels)
> - `CONS` - Consumables (Cleaning supplies, Office items)

---

### 1.3 Sub Group Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `SubGroupMaster` |
| **Table Name** | `sub_group_master` |
| **Primary Key** | Composite (`group_id` + `sub_group_id`) |

#### Business Name
**Material Sub-Category**

#### Purpose
Second-level classification within a group. Provides finer granularity for organizing items (e.g., within "Dairy", sub-groups like "Milk", "Curd", "Paneer").

#### When is it created?
- When defining detailed classifications within a group
- When a new product line is introduced within an existing category

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | More specific filtering |
| Operations | Detailed stock segregation |
| Reports | Drill-down analysis |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Parent Group | `group_id` | Link to parent category |
| Sub-Group Code | `sub_group_id` | Unique within the group (e.g., "MILK") |
| Sub-Group Name | `sub_group_desc` | Description (e.g., "Fresh Milk Products") |

#### Stock Impact
**No stock impact** - Classification only.

#### Accounting Relevance
- **Detailed Reporting:** Sub-category wise valuation

#### Example
> **Scenario:** Under "DAIRY" group, create sub-groups:
> - `DAIRY` / `MILK` - Fresh Milk Products
> - `DAIRY` / `CURD` - Curd & Yogurt
> - `DAIRY` / `PANR` - Paneer & Cheese

---

### 1.4 Unit Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `UnitMaster` |
| **Table Name** | `unit_master` |
| **Primary Key** | `unit_id` (VARCHAR 20) |

#### Business Name
**Unit of Measurement (UOM)**

#### Purpose
Defines how items are measured and counted. Critical for accurate stock tracking, ordering, and issuing.

#### When is it created?
- During initial system setup
- When introducing items with new measurement units

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Specify quantity in orders |
| Store | Count and issue materials |
| Finance | Calculate values correctly |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Unit Code | `unit_id` | Short code (e.g., "LTR", "KG", "PCS") |
| Unit Name | `unit_desc` | Full description (e.g., "Liters", "Kilograms") |

#### Stock Impact
**No stock impact** - Reference data.

#### Accounting Relevance
- **Accuracy:** Ensures quantities are calculated correctly
- **GST:** Correct UOM required in tax invoices

#### Example
> **Common Units:**
> - `LTR` - Liters (for liquids)
> - `KG` - Kilograms (for bulk items)
> - `PCS` - Pieces (for countable items)
> - `PKT` - Packets
> - `BOX` - Boxes

---

### 1.5 Manufacturer Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `ManufacturerMaster` |
| **Table Name** | `manufacturer_master` |
| **Primary Key** | `manuf_id` (VARCHAR 20) |

#### Business Name
**Brand / Manufacturer**

#### Purpose
Stores information about companies that manufacture the items. Different from suppliers (who may be distributors).

#### When is it created?
- When onboarding a new brand
- When sourcing from a new manufacturer

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Track brand preferences |
| Quality | Identify source for quality issues |
| Marketing | Brand-wise analysis |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Manufacturer ID | `manuf_id` | Unique code (e.g., "AMUL", "MOTHER") |
| Name | `manuf_name` | Full name (e.g., "Amul Dairy Cooperative") |
| Address | `address_1`, `address_2`, `address_3` | Factory/office location |
| Contact | `phone_1`, `email` | For quality or supply issues |

#### Stock Impact
**No stock impact** - Reference data.

#### Accounting Relevance
- **None directly** - For operational tracking

#### Example
> **Scenario:** Adding Amul as a manufacturer:
> - ID: `AMUL`
> - Name: `Gujarat Cooperative Milk Marketing Federation`
> - Address: `Amul Dairy Road, Anand, Gujarat`
> - Phone: `+91-2692-258506`

---

### 1.6 Supplier Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `SupplierMaster` |
| **Table Name** | `supplier_master` |
| **Primary Key** | `supp_id` (VARCHAR 20) |

#### Business Name
**Vendor / Supplier**

#### Purpose
Stores information about vendors from whom materials are purchased. Critical for purchase orders, invoices, and payments.

#### When is it created?
- When onboarding a new vendor
- Before placing the first purchase order

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Create POs, track vendor performance |
| Finance | Process payments, verify invoices |
| Store | Identify material source during GRN |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Supplier ID | `supp_id` | Unique code (e.g., "SUP001") |
| Supplier Name | `supp_name` | Legal/business name |
| GSTIN | `gstin` | Tax registration (mandatory for GST) |
| Contact Person | `cont_pers` | Primary point of contact |
| Mobile | `mob_no` | Quick contact number |
| Email | `email` | For POs and communication |
| Address | `address_1`, `address_2`, `address_3` | Business address |

#### Stock Impact
**No stock impact** - Reference data.

#### Accounting Relevance
- **Critical for GST:** GSTIN required for input tax credit
- **Payments:** Supplier details needed for processing payments
- **Audit:** Verify supplier legitimacy

#### Example
> **Scenario:** Adding a packaging supplier:
> - ID: `SUP045`
> - Name: `Packwell Industries Pvt Ltd`
> - GSTIN: `29AABCP1234X1ZA`
> - Contact: `Mr. Rajesh Kumar`
> - Mobile: `9876543210`
> - Email: `orders@packwell.com`

---

### 1.7 Item Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `ItemMaster` |
| **Table Name** | `item_master` |
| **Primary Key** | `item_id` (VARCHAR 20) |

#### Business Name
**Material / SKU / Product**

#### Purpose
The heart of the MMS. Stores every material that can be purchased, stocked, or issued. Contains pricing, tax, and classification information.

#### When is it created?
- When introducing a new material
- Before the first purchase or stock entry

#### Who uses it?
| Team | Usage |
|------|-------|
| Everyone | Reference for all transactions |
| Purchase | Create POs with correct pricing |
| Store | Identify items during GRN/Issue |
| Finance | Tax calculations, valuations |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Item Code | `item_id` | Unique SKU code (e.g., "MLK001") |
| Description | `item_desc` | Full item name (e.g., "Toned Milk 500ml Pouch") |
| Group/Sub-Group | `group_id`, `sub_group_id` | Category classification |
| Unit | `unit_id` | How it's measured (PCS, LTR, KG) |
| Reference Cost | `cost_price` | **Reference only** - actual price from last GRN |
| MRP | `mrp` | Maximum retail price (if applicable) |
| GST % | `gst_perc` | Tax rate (5%, 12%, 18%, etc.) |
| HSN Code | `hsn_code` | GST classification code |
| Manufacturer | `manuf_id` | Who makes this item |

#### Stock Impact
**No direct stock impact** - But all stock is tracked against items.

#### Accounting Relevance
- **Tax:** GST rate, HSN code for compliance
- **Valuation:** Reference pricing for variance analysis
- **Costing:** Cost price affects P&L

#### Example
> **Scenario:** Adding a new milk product:
> - ID: `MLK001`
> - Description: `Farm Fresh Full Cream Milk 500ml`
> - Group: `DAIRY` / Sub-Group: `MILK`
> - Unit: `PCS`
> - Cost Price: `₹25.00` (reference)
> - GST: `5%`
> - HSN: `0401`

---

### 1.8 Location Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `LocationMaster` |
| **Table Name** | `location_master` |
| **Primary Key** | `location_id` (VARCHAR 20) |

#### Business Name
**Storage Location / Bin / Rack**

#### Purpose
Defines storage locations within a branch. Enables precise tracking of where materials are physically stored (which rack, shelf, cold room, etc.).

#### When is it created?
- When setting up a new branch
- When adding new storage areas (new rack, cold room, etc.)

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Know exact physical location of items |
| Operations | Optimize warehouse layout |
| Picking | Quick retrieval during issue |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Location ID | `location_id` | Unique code (e.g., "COLD-01", "RACK-A1") |
| Branch | `branch_id` | Which branch this location belongs to |
| Location Name | `location_name` | Description (e.g., "Cold Room 1", "Rack A Row 1") |
| Parent Location | `parent_id` | Hierarchical structure (optional) |

#### Stock Impact
**No direct impact** - Stock tracked at location level in ledger.

#### Accounting Relevance
- **Physical Verification:** Enables accurate stock audits
- **Inventory Control:** Location-wise stock valuation

#### Example
> **Scenario:** Bangalore warehouse has:
> - `BLR-COLD01` - Cold Storage Room 1 (for perishables)
> - `BLR-RACK-A1` - Rack A, Row 1 (for packaging)
> - `BLR-BULK01` - Bulk Storage Area 1

---

## 2. Purchase

Purchase tables track the ordering process from purchase order creation to supplier invoicing.

---

### 2.1 PO Header

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `PoHeader` |
| **Table Name** | `po_header` |
| **Primary Key** | `po_id` (BIGINT, Auto-generated) |

#### Business Name
**Purchase Order (PO)**

#### Purpose
Records the header-level information of a purchase order sent to a supplier. Contains who, when, and where of the order.

#### When is it created?
- When purchase team raises a new order
- Based on stock requirements or reorder levels

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Track pending orders, supplier communication |
| Store | Know what materials are expected |
| Finance | Verify invoices against POs |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| PO Number | `po_id` | Unique order number |
| Branch | `branch_id` | Which branch will receive the goods |
| Supplier | `supp_id` | Who we're ordering from |
| PO Date | `po_date` | When the order was placed |
| Status | `status` | OPEN / PARTIAL / CLOSED |
| Created By | `created_by` | Who raised the PO |

#### Stock Impact
**No stock impact** - PO is just an intent to purchase. Stock increases only on GRN.

#### Accounting Relevance
- **Commitment:** Represents a financial commitment
- **Audit Trail:** Links orders to receipts and invoices
- **Three-way Match:** PO ↔ GRN ↔ Invoice verification

#### Example
> **Scenario:** Purchase team orders milk pouches:
> - PO ID: `1001`
> - Branch: `BLR01`
> - Supplier: `SUP001`
> - Date: `2026-02-06`
> - Status: `OPEN`
> - Created By: `purchase_mgr@cd.com`

---

### 2.2 PO Detail

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `PoDetail` |
| **Table Name** | `po_detail` |
| **Primary Key** | Composite (`po_id` + `item_id`) |

#### Business Name
**Purchase Order Line Item**

#### Purpose
Stores individual items in a purchase order - what items, how many, at what price.

#### When is it created?
- Along with PO Header
- One record per item ordered

#### Who uses it?
| Team | Usage |
|------|-------|
| Purchase | Track ordered quantities |
| Store | Verify received vs ordered |
| Finance | Check pricing matches invoices |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| PO Reference | `po_id` | Links to PO Header |
| Item | `item_id` | Which material |
| Qty Ordered | `qty_ordered` | How much we ordered |
| Rate | `rate` | Agreed price per unit |
| Qty Received | `qty_received` | How much received so far (updated by GRN) |

#### Stock Impact
**No stock impact** - Detail of the intent.

#### Accounting Relevance
- **Price Tracking:** Compare ordered price vs invoiced price
- **Pending Analysis:** qty_ordered - qty_received = pending

#### Example
> **Scenario:** PO 1001 contains:
> - Item: `MLK001`, Qty: 1000, Rate: ₹24.50, Received: 0
> - Item: `MLK002`, Qty: 500, Rate: ₹28.00, Received: 0

---

### 2.3 Supplier Invoice

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `SupplierInvoice` |
| **Table Name** | `supplier_invoice` |
| **Primary Key** | `invoice_id` (BIGINT, Auto-generated) |

#### Business Name
**Vendor Bill / Supplier Invoice**

#### Purpose
Records invoices received from suppliers. Links to GRN for three-way matching (PO ↔ GRN ↔ Invoice).

#### When is it created?
- When supplier sends an invoice
- During or after GRN entry

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Attach invoice to GRN |
| Finance | Process payments |
| Audit | Verify documentation |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Invoice ID | `invoice_id` | System generated reference |
| Supplier | `supp_id` | Which vendor's invoice |
| Invoice Number | `invoice_no` | Supplier's invoice number |
| Invoice Date | `invoice_date` | Date on the invoice |
| Invoice Amount | `invoice_amount` | Base amount before tax |
| GST Amount | `gst_amount` | Tax charged |
| Net Amount | `net_amount` | Total payable |
| Invoice Copy | `invoice_s3_url` | Link to scanned invoice (AWS S3) |

#### Stock Impact
**No stock impact** - Financial document only.

#### Accounting Relevance
- **Critical for Payments:** Basis for payment processing
- **GST Input Credit:** Required for claiming tax credit
- **Audit:** Must match physical goods received

#### Example
> **Scenario:** Invoice from Packwell Industries:
> - Supplier Invoice No: `PWI/2026/1234`
> - Date: `2026-02-05`
> - Amount: ₹50,000
> - GST: ₹9,000 (18%)
> - Net: ₹59,000
> - S3 URL: `s3://mms-invoices/2026/02/PWI_1234.pdf`

---

## 3. Inward

Inward tables record materials entering a branch. **GRN is the ONLY way to increase stock.**

---

### 3.1 GRN Header

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `GrnHeader` |
| **Table Name** | `grn_header` |
| **Primary Key** | `grn_id` (BIGINT, Auto-generated) |

#### Business Name
**Goods Receipt Note (GRN) / Material Inward**

#### Purpose
Records the receipt of materials at a branch. **This is the ONLY transaction that increases stock.** Links to PO and Invoice for complete traceability.

#### When is it created?
- When materials physically arrive at the branch
- Store person verifies and accepts the delivery

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Record incoming materials |
| Purchase | Track PO fulfillment |
| Finance | Three-way matching, vendor payments |
| Audit | Verify receipt documentation |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| GRN Number | `grn_id` | Unique receipt number |
| Branch | `branch_id` | Where goods were received |
| Supplier | `supp_id` | Who delivered |
| PO Reference | `po_id` | Against which PO (optional) |
| Invoice Reference | `invoice_id` | Linked supplier invoice |
| Challan No | `challan_no` | Delivery challan number |
| GRN Date | `grn_date` | When goods were received |
| Status | `status` | DRAFT / PENDING_APPROVAL / POSTED |
| Approved By | `approved_by` | Who approved (if required) |

#### Stock Impact
**INCREASES STOCK** - When GRN is posted, stock ledger records inward.

#### Accounting Relevance
- **Inventory Value:** Increases inventory asset
- **Payment Trigger:** GRN completion enables payment
- **Audit Trail:** Links delivery to order to invoice

#### Example
> **Scenario:** Bangalore receives milk delivery:
> - GRN ID: `5001`
> - Branch: `BLR01`
> - Supplier: `SUP001`
> - PO: `1001`
> - Challan: `DC/2026/789`
> - Date: `2026-02-06`
> - Status: `POSTED`

---

### 3.2 GRN Detail

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `GrnDetail` |
| **Table Name** | `grn_detail` |
| **Primary Key** | Composite (`grn_id` + `item_id`) |

#### Business Name
**GRN Line Item / Received Material**

#### Purpose
Stores each item received in a GRN - what, how much, at what price, and where it was stored. Contains the actual price (which may differ from PO).

#### When is it created?
- Along with GRN Header
- One record per item received

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Record actual receipt quantities and location |
| Finance | Actual cost for inventory valuation |
| FIFO | Track batches for consumption |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| GRN Reference | `grn_id` | Links to GRN Header |
| Item | `item_id` | Which material received |
| Unit | `unit_id` | Measurement unit |
| Location | `location_id` | Where stored (rack, cold room) |
| Qty Received | `qty_received` | How much actually received |
| Rate | `rate` | **Actual price** (may differ from PO) |
| GST % | `gst_perc` | Tax rate applied |
| Line Amount | `line_amount` | qty × rate |
| Qty Remaining | `qty_remaining` | For FIFO - how much still available |

#### Stock Impact
**INCREASES STOCK** - Each line adds to inventory.

#### Accounting Relevance
- **Actual Cost:** Rate here determines inventory valuation
- **FIFO Basis:** qty_remaining tracks available batch quantity
- **Variance:** Compare with PO rate for price variance

#### Example
> **Scenario:** GRN 5001 contains:
> - Item: `MLK001`, Received: 980, Rate: ₹25.00, Location: `BLR-COLD01`
> - Item: `MLK002`, Received: 500, Rate: ₹28.50, Location: `BLR-COLD01`

---

## 4. Outward

Outward tables record materials leaving a branch. **Issue is the ONLY way to decrease stock (besides transfers).**

---

### 4.1 Issue Header

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `IssueHeader` |
| **Table Name** | `issue_header` |
| **Primary Key** | `issue_id` (BIGINT, Auto-generated) |

#### Business Name
**Material Issue / Stock Issue / Requisition**

#### Purpose
Records when materials are issued from stock - to production, a department, or for consumption. Uses FIFO to determine which batches to consume.

#### When is it created?
- When someone requests materials from the store
- Approved requisition triggers issue

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Record outgoing materials |
| Requesting Dept | Track received materials |
| Finance | Cost allocation to departments |
| Audit | Verify material consumption |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Issue Number | `issue_id` | Unique issue reference |
| Branch | `branch_id` | Which branch issued |
| Issue Date | `issue_date` | When issued |
| Issued To | `issued_to` | Recipient (person/dept/purpose) |
| Status | `status` | DRAFT / PENDING_APPROVAL / POSTED |
| Remarks | `remarks` | Purpose or notes |
| Approved By | `approved_by` | Who approved |

#### Stock Impact
**DECREASES STOCK** - When posted, stock ledger records outward.

#### Accounting Relevance
- **Cost Allocation:** Materials charged to department/project
- **Consumption:** Reduces inventory asset
- **FIFO Compliance:** Ensures old stock used first

#### Example
> **Scenario:** Production requests milk for today's batch:
> - Issue ID: `8001`
> - Branch: `BLR01`
> - Date: `2026-02-06`
> - Issued To: `Production - Morning Batch`
> - Status: `POSTED`

---

### 4.2 Issue Detail

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `IssueDetail` |
| **Table Name** | `issue_detail` |
| **Primary Key** | Composite (`issue_id` + `item_id`) |

#### Business Name
**Issue Line Item / Issued Material**

#### Purpose
Stores each item issued - what, how much, from where, and at what rate. Rate is calculated using FIFO from GRN batches.

#### When is it created?
- Along with Issue Header
- One record per item issued

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Record quantities issued |
| Finance | Calculate issue cost |
| FIFO Service | Determine batch consumption |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Issue Reference | `issue_id` | Links to Issue Header |
| Item | `item_id` | Which material issued |
| Location | `location_id` | From which storage location |
| Qty Issued | `qty_issued` | How much issued |
| Rate | `rate` | **Weighted average FIFO rate** |

#### Stock Impact
**DECREASES STOCK** - Each line reduces inventory.

#### Accounting Relevance
- **FIFO Cost:** Rate calculated from oldest batches
- **Weighted Average:** If multiple batches consumed
- **Cost of Goods:** Impacts COGS calculation

#### Example
> **Scenario:** Issue 8001 contains:
> - Item: `MLK001`, Qty: 200, Location: `BLR-COLD01`, Rate: ₹24.75 (FIFO avg)

---

### 4.3 Issue FIFO Consumption

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `IssueFifoConsumption` |
| **Table Name** | `issue_fifo_consumption` |
| **Primary Key** | `consumption_id` (BIGINT, Auto-generated) |

#### Business Name
**Batch Consumption Record / FIFO Trail**

#### Purpose
**Audit table** that records exactly which GRN batches were consumed for each issue. Enables complete traceability from issue back to original receipt.

#### When is it created?
- Automatically when Issue is posted
- System calculates FIFO and records consumption

#### Who uses it?
| Team | Usage |
|------|-------|
| System | FIFO calculation |
| Audit | Trace material source |
| Finance | Verify costing accuracy |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Issue Reference | `issue_id`, `item_id` | Which issue line |
| GRN Reference | `grn_id` | Which GRN batch was consumed |
| Qty Consumed | `qty_consumed` | How much from this batch |
| Rate | `rate` | Rate of this GRN batch |

#### Stock Impact
**No additional impact** - Records the breakdown of Issue Detail.

#### Accounting Relevance
- **FIFO Proof:** Demonstrates oldest batches used first
- **Audit Trail:** Complete traceability
- **Weighted Average:** Sum(qty × rate) / Sum(qty) = Issue rate

#### Example
> **Scenario:** Issue line consumed from multiple GRNs:
> - Issue 8001, Item MLK001:
>   - From GRN 4998: 50 units @ ₹24.00
>   - From GRN 5001: 150 units @ ₹25.00
>   - Weighted avg: (50×24 + 150×25) / 200 = ₹24.75

---

## 5. Transfer

Transfer tables manage stock movements between branches. Implemented as Issue from source + GRN at destination.

---

### 5.1 Stock Transfer Header

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `StockTransferHeader` |
| **Table Name** | `stock_transfer_header` |
| **Primary Key** | `transfer_id` (BIGINT, Auto-generated) |

#### Business Name
**Inter-Branch Transfer / Stock Transfer Order (STO)**

#### Purpose
Records movement of materials from one branch to another. Links to Issue (sender) and GRN (receiver) to maintain proper stock trail.

#### When is it created?
- When one branch requests stock from another
- Approved by both sending and receiving branches

#### Who uses it?
| Team | Usage |
|------|-------|
| Operations | Manage inter-branch logistics |
| Both Branches | Track sent/received materials |
| Finance | Internal stock movement reporting |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Transfer ID | `transfer_id` | Unique transfer number |
| From Branch | `from_branch` | Sending branch |
| To Branch | `to_branch` | Receiving branch |
| Transfer Date | `transfer_date` | When initiated |
| Status | `status` | CREATED / IN_TRANSIT / RECEIVED |
| Sender Issue | `sender_issue_id` | Issue at sending branch |
| Receiver GRN | `receiver_grn_id` | GRN at receiving branch |

#### Stock Impact
- **Source Branch:** Decreases (via linked Issue)
- **Destination Branch:** Increases (via linked GRN)
- **Net Company Level:** Zero change

#### Accounting Relevance
- **Internal Movement:** No external financial impact
- **Branch Valuation:** Affects individual branch inventory
- **Audit:** Complete trail of inter-branch movements

#### Example
> **Scenario:** Delhi requests milk from Bangalore:
> - Transfer ID: `2001`
> - From: `BLR01`
> - To: `DEL01`
> - Status: `IN_TRANSIT`
> - Sender Issue: `8002`
> - Receiver GRN: `null` (pending receipt)

---

### 5.2 Stock Transfer Detail

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `StockTransferDetail` |
| **Table Name** | `stock_transfer_detail` |
| **Primary Key** | Composite (`transfer_id` + `item_id`) |

#### Business Name
**Transfer Line Item**

#### Purpose
Stores each item in the transfer - what, how much sent, and how much received. Handles discrepancies between sent and received quantities.

#### When is it created?
- Along with Transfer Header
- One record per item transferred

#### Who uses it?
| Team | Usage |
|------|-------|
| Operations | Track quantities in transit |
| Quality | Investigate discrepancies |
| Finance | Reconcile branch inventories |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Transfer Reference | `transfer_id` | Links to Transfer Header |
| Item | `item_id` | Which material |
| Qty Sent | `qty_sent` | How much dispatched |
| Rate | `rate` | Transfer value (FIFO from Issue) |
| Qty Received | `qty_received` | How much actually received |

#### Stock Impact
- **Discrepancy Handling:** qty_sent ≠ qty_received triggers investigation

#### Accounting Relevance
- **Transfer Price:** Usually at FIFO cost
- **Loss Detection:** Sent vs Received variance

#### Example
> **Scenario:** Transfer 2001 items:
> - Item: `MLK001`, Sent: 500, Rate: ₹24.80, Received: 498 (2 damaged)

---

## 6. Stock

Stock tables are the **SINGLE SOURCE OF TRUTH** for inventory. The ledger is append-only.

---

### 6.1 Material Stock Ledger

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `MaterialStockLedger` |
| **Table Name** | `material_stock_ledger` |
| **Primary Key** | `ledger_id` (BIGINT, Auto-generated) |

#### Business Name
**Stock Ledger / Inventory Journal / Stock Movement Register**

#### Purpose
**THE SINGLE SOURCE OF TRUTH.** Every stock movement (in or out) is recorded here as an immutable entry. No updates, no deletes - only inserts. Running balance maintained per entry.

#### When is it created?
- Automatically when GRN is posted (stock in)
- Automatically when Issue is posted (stock out)
- Automatically on Transfer dispatch/receipt

#### Who uses it?
| Team | Usage |
|------|-------|
| System | Calculate current stock |
| Finance | Stock valuation, aging reports |
| Audit | Complete movement history |
| Operations | Trace any stock discrepancy |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Entry ID | `ledger_id` | Unique, sequential entry number |
| Branch | `branch_id` | Which branch |
| Item | `item_id` | Which material |
| Location | `location_id` | Storage location |
| Transaction Date | `txn_date` | When movement occurred |
| Transaction Type | `txn_type` | GRN / ISSUE / TRANSFER_IN / TRANSFER_OUT |
| Reference | `ref_id` | Links to GRN/Issue/Transfer ID |
| Qty In | `qty_in` | Quantity added (for GRN) |
| Qty Out | `qty_out` | Quantity removed (for Issue) |
| Rate | `rate` | Unit price at this transaction |
| Balance | `balance_qty` | Running balance after this entry |

#### Stock Impact
**This IS the stock** - All stock calculations derive from this table.

#### Accounting Relevance
- **Inventory Valuation:** Base for all stock reports
- **FIFO Tracking:** txn_date determines age
- **Aging Reports:** Derived from txn_date
- **Audit-Ready:** Immutable, complete history

#### Example
> **Sample Ledger Entries:**
> | ID | Branch | Item | Type | Qty In | Qty Out | Rate | Balance |
> |----|--------|------|------|--------|---------|------|---------|
> | 1001 | BLR01 | MLK001 | GRN | 1000 | 0 | 24.00 | 1000 |
> | 1002 | BLR01 | MLK001 | ISSUE | 0 | 200 | 24.00 | 800 |
> | 1003 | BLR01 | MLK001 | GRN | 500 | 0 | 25.00 | 1300 |

---

### 6.2 Branch Material Stock

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `BranchMaterialStock` |
| **Table Name** | `branch_material_stock` |
| **Primary Key** | Composite (`branch_id` + `item_id` + `location_id`) |

#### Business Name
**Current Stock / Stock on Hand / Inventory Summary**

#### Purpose
**Denormalized summary table** for quick stock lookups. Derived from Stock Ledger. Provides current quantity and average cost without querying the entire ledger.

#### When is it created?
- Initially when first GRN posts for an item
- Updated on every stock movement

#### Who uses it?
| Team | Usage |
|------|-------|
| Store | Quick stock check |
| Purchase | Reorder decisions |
| Operations | Availability verification |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Branch | `branch_id` | Which branch |
| Item | `item_id` | Which material |
| Location | `location_id` | Storage location |
| Qty on Hand | `qty_on_hand` | Current available quantity |
| Average Cost | `avg_cost` | Running average unit cost |
| Last Updated | `last_updated` | When last movement occurred |

#### Stock Impact
**Reflects current stock** - Summary of ledger state.

#### Accounting Relevance
- **Quick Valuation:** qty × avg_cost = stock value
- **Reorder Point:** Compare against minimum levels
- **Performance:** Fast queries vs aggregating ledger

#### Example
> **Current Stock View:**
> | Branch | Item | Location | Qty | Avg Cost | Value |
> |--------|------|----------|-----|----------|-------|
> | BLR01 | MLK001 | BLR-COLD01 | 1300 | ₹24.38 | ₹31,694 |
> | BLR01 | MLK002 | BLR-COLD01 | 480 | ₹28.50 | ₹13,680 |

---

## 7. Governance

Governance tables control access, roles, and approval workflows.

---

### 7.1 Role Master

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `RoleMaster` |
| **Table Name** | `role_master` |
| **Primary Key** | `role_id` (VARCHAR 20) |

#### Business Name
**User Role / Permission Level**

#### Purpose
Defines roles in the system (e.g., Store Manager, Purchase Officer). Roles determine what actions a user can perform.

#### When is it created?
- During system setup
- When new organizational roles are defined

#### Who uses it?
| Team | Usage |
|------|-------|
| Admin | Define permission structure |
| System | Access control |
| Audit | Verify role segregation |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Role ID | `role_id` | Unique code (e.g., "STORE_MGR") |
| Role Name | `role_name` | Description (e.g., "Store Manager") |

#### Stock Impact
**No stock impact** - Security configuration.

#### Accounting Relevance
- **Segregation of Duties:** Enforces control

#### Example
> **Common Roles:**
> - `STORE_EXEC` - Store Executive (create GRN/Issue)
> - `STORE_MGR` - Store Manager (approve GRN/Issue)
> - `PURCHASE` - Purchase Officer (create PO)
> - `FINANCE` - Finance Team (view reports, approve payments)

---

### 7.2 User Role Map

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `UserRoleMap` |
| **Table Name** | `user_role_map` |
| **Primary Key** | Composite (`user_id` + `role_id`) |

#### Business Name
**User-Role Assignment**

#### Purpose
Maps users to their roles. A user can have multiple roles. Controls access permissions.

#### When is it created?
- When onboarding a new user
- When changing user responsibilities

#### Who uses it?
| Team | Usage |
|------|-------|
| Admin | Manage user access |
| System | Authenticate and authorize |
| Audit | Review access controls |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| User ID | `user_id` | Username or employee ID |
| Role | `role_id` | Assigned role |

#### Stock Impact
**No stock impact** - Security configuration.

#### Accounting Relevance
- **Audit:** Who had what access when

#### Example
> **Sample Assignments:**
> - `ram@cd.com` → `STORE_EXEC`
> - `ram@cd.com` → `PURCHASE`
> - `shyam@cd.com` → `STORE_MGR`

---

### 7.3 Approval Rule

#### Technical Details
| Attribute | Value |
|-----------|-------|
| **Java Entity** | `ApprovalRule` |
| **Table Name** | `approval_rule` |
| **Primary Key** | `rule_id` (VARCHAR 20) |

#### Business Name
**Approval Policy / Authorization Matrix**

#### Purpose
Defines when approvals are required. Based on transaction type (GRN, Issue) and conditions (price variance, quantity variance). Enforces financial controls.

#### When is it created?
- During system setup
- When policies change

#### Who uses it?
| Team | Usage |
|------|-------|
| System | Check if approval needed |
| Approvers | Know their thresholds |
| Audit | Verify policy compliance |

#### Key Fields (Business Important)

| Field | Technical Name | Description |
|-------|---------------|-------------|
| Rule ID | `rule_id` | Unique rule code |
| Transaction Type | `txn_type` | GRN / ISSUE / TRANSFER |
| Condition Type | `condition_type` | PRICE_VARIANCE / QTY_VARIANCE |
| Threshold | `threshold_value` | When rule triggers (e.g., 5% variance) |
| Required Role | `required_role` | Who must approve |

#### Stock Impact
**No direct stock impact** - Controls stock transactions.

#### Accounting Relevance
- **Financial Control:** Prevents unauthorized price changes
- **Compliance:** Enforces approval policies
- **Audit:** Demonstrates control effectiveness

#### Example
> **Sample Rules:**
> | Rule | Type | Condition | Threshold | Approver |
> |------|------|-----------|-----------|----------|
> | R001 | GRN | PRICE_VARIANCE | 5% | STORE_MGR |
> | R002 | GRN | PRICE_VARIANCE | 10% | FINANCE |
> | R003 | ISSUE | QTY_VARIANCE | 100 units | STORE_MGR |

---

## Appendix: Entity Relationships

```
                    ┌─────────────────┐
                    │  GROUP_MASTER   │
                    └────────┬────────┘
                             │ 1:N
                    ┌────────▼────────┐
                    │ SUB_GROUP_MASTER│
                    └────────┬────────┘
                             │ N:1
┌──────────────┐    ┌────────▼────────┐    ┌──────────────┐
│ MANUF_MASTER │◄───│   ITEM_MASTER   │───►│ UNIT_MASTER  │
└──────────────┘    └────────┬────────┘    └──────────────┘
                             │
        ┌────────────────────┼────────────────────┐
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   PO_DETAIL   │    │  GRN_DETAIL   │    │ ISSUE_DETAIL  │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        ▼                    ▼                    ▼
┌───────────────┐    ┌───────────────┐    ┌───────────────┐
│   PO_HEADER   │    │  GRN_HEADER   │    │ ISSUE_HEADER  │
└───────┬───────┘    └───────┬───────┘    └───────┬───────┘
        │                    │                    │
        └────────────────────┼────────────────────┘
                             │
                    ┌────────▼────────┐
                    │ SUPPLIER_MASTER │
                    └─────────────────┘


┌─────────────────┐         ┌─────────────────┐
│  BRANCH_MASTER  │◄───────►│ LOCATION_MASTER │
└────────┬────────┘         └────────┬────────┘
         │                           │
         └───────────┬───────────────┘
                     │
            ┌────────▼────────┐
            │MATERIAL_STOCK_  │
            │    LEDGER       │ (Single Source of Truth)
            └────────┬────────┘
                     │ Derived
            ┌────────▼────────┐
            │ BRANCH_MATERIAL_│
            │     STOCK       │ (Summary View)
            └─────────────────┘
```

---

## Quick Reference: Stock Impact Summary

| Transaction | Stock Impact | Table Updated |
|-------------|--------------|---------------|
| Create PO | No change | po_header, po_detail |
| Create GRN (Draft) | No change | grn_header, grn_detail |
| **Post GRN** | **Increases Stock** | material_stock_ledger, branch_material_stock |
| Create Issue (Draft) | No change | issue_header, issue_detail |
| **Post Issue** | **Decreases Stock** | material_stock_ledger, branch_material_stock, issue_fifo_consumption |
| **Transfer Dispatch** | **Decreases at Source** | stock_transfer_*, material_stock_ledger |
| **Transfer Receive** | **Increases at Destination** | stock_transfer_*, material_stock_ledger |

---

## Key Business Rules

1. **Ledger is Truth:** Stock ledger is append-only. No updates. No deletes.
2. **GRN = Stock In:** Only GRN can increase stock.
3. **Issue = Stock Out:** Only Issue/Transfer can decrease stock.
4. **FIFO Mandatory:** Oldest stock consumed first.
5. **Price from Ledger:** Actual price comes from last GRN, not item master.
6. **Approval Enforced:** Price/quantity variances require manager approval.
7. **Transfer = Issue + GRN:** Inter-branch transfer creates Issue at source and GRN at destination.
8. **Aging from Date:** Stock aging calculated from ledger txn_date.

---

*Document generated for Material Management System v1.0*
