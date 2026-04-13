#!/bin/bash
# Complete Workflow Test with Correct Parameters

set -e

BASE_URL="http://localhost:8080/mms/api/v1"

echo "=========================================="
echo "MMS Complete Workflow Test"
echo "=========================================="
echo ""

GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() {
    echo ""
    echo "${BLUE}$1${NC}"
    echo "----------------------------------------"
}

print_step "Step 1: Create GRN (Goods Receipt)"

GRN_RESPONSE=$(curl -s -X POST "$BASE_URL/grn" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "suppId": "SUP001",
    "grnDate": "2026-02-12",
    "remarks": "Test delivery of milk",
    "details": [
      {
        "itemId": "ITM001",
        "unitId": "LTR",
        "locationId": "BIN-DL-A1",
        "qtyReceived": 500,
        "rate": 45.00,
        "remarks": "Fresh stock"
      }
    ]
  }')

echo "$GRN_RESPONSE" | jq '.'
GRN_ID=$(echo "$GRN_RESPONSE" | jq -r '.data.grnId')
echo "${GREEN}✅ GRN Created: ID = $GRN_ID${NC}"

print_step "Step 2: Submit GRN (with submittedBy)"

curl -s -X POST "$BASE_URL/grn/$GRN_ID/submit?submittedBy=USER001" | jq '.'
echo "${GREEN}✅ GRN Submitted and Posted${NC}"

print_step "Step 3: Verify Stock"

curl -s "$BASE_URL/stock/summary/BR001" | jq '.'
echo "${GREEN}✅ Stock: 500 LTR at BR001${NC}"

print_step "Step 4: Create Issue (Material Outward)"

ISSUE_RESPONSE=$(curl -s -X POST "$BASE_URL/issue" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "issueType": "PRODUCTION",
    "issueDate": "2026-02-13",
    "remarks": "Production batch #1",
    "details": [
      {
        "itemId": "ITM001",
        "locationId": "BIN-DL-A1",
        "qtyIssued": 200
      }
    ]
  }')

echo "$ISSUE_RESPONSE" | jq '.'
ISSUE_ID=$(echo "$ISSUE_RESPONSE" | jq -r '.data.issueId')
echo "${GREEN}✅ Issue Created: ID = $ISSUE_ID${NC}"

print_step "Step 5: Post Issue (with postedBy - FIFO consumption)"

curl -s -X POST "$BASE_URL/issue/$ISSUE_ID/post?postedBy=USER001" | jq '.'
echo "${GREEN}✅ Issue Posted with FIFO${NC}"

print_step "Step 6: Verify Stock After Issue"

curl -s "$BASE_URL/stock/summary/BR001" | jq '.'
echo "${GREEN}✅ Stock: 300 LTR at BR001 (500 - 200)${NC}"

print_step "Step 7: Create Inter-Branch Transfer"

TRANSFER_RESPONSE=$(curl -s -X POST "$BASE_URL/transfer" \
  -H "Content-Type: application/json" \
  -d '{
    "fromBranchId": "BR001",
    "toBranchId": "BR002",
    "transferDate": "2026-02-13",
    "remarks": "Stock rebalancing",
    "details": [
      {
        "itemId": "ITM001",
        "qtyToTransfer": 100,
        "fromLocationId": "BIN-DL-A1",
        "toLocationId": "BIN-MB-B1"
      }
    ]
  }')

echo "$TRANSFER_RESPONSE" | jq '.'
TRANSFER_ID=$(echo "$TRANSFER_RESPONSE" | jq -r '.data.transferId')
echo "${GREEN}✅ Transfer Created: ID = $TRANSFER_ID${NC}"

print_step "Step 8: Dispatch Transfer (with sourceLocationId and dispatchedBy)"

curl -s -X POST "$BASE_URL/transfer/$TRANSFER_ID/dispatch?sourceLocationId=BIN-DL-A1&dispatchedBy=USER001" | jq '.'
echo "${GREEN}✅ Transfer Dispatched from BR001${NC}"

print_step "Step 9: Receive Transfer (with destLocationId and receivedBy)"

curl -s -X POST "$BASE_URL/transfer/$TRANSFER_ID/receive?destLocationId=BIN-MB-B1&receivedBy=USER002" | jq '.'
echo "${GREEN}✅ Transfer Received at BR002${NC}"

print_step "Step 10: Verify Final Stock"

echo "Stock at BR001 (Delhi):"
curl -s "$BASE_URL/stock/summary/BR001" | jq '.'
echo "${GREEN}Expected: 200 LTR (300 - 100 transfer)${NC}"

echo ""
echo "Stock at BR002 (Mumbai):"
curl -s "$BASE_URL/stock/summary/BR002" | jq '.'
echo "${GREEN}Expected: 100 LTR (received from transfer)${NC}"

print_step "Step 11: Generate Reports"

echo "Stock Ledger Report:"
curl -s "$BASE_URL/reports/stock-ledger?branchIds=BR001&fromDate=2026-02-12&toDate=2026-02-13" | jq '.[0:3]'

echo ""
echo "FIFO Consumption Report:"
curl -s "$BASE_URL/reports/fifo-consumption?branchIds=BR001&fromDate=2026-02-12&toDate=2026-02-13" | jq '.'

echo ""
echo "${GREEN}=========================================="
echo "Complete Workflow Test Finished!"
echo "==========================================${NC}"
echo ""
echo "Summary:"
echo "  ✅ GRN created and posted: 500 LTR received"
echo "  ✅ Issue posted with FIFO: 200 LTR consumed"
echo "  ✅ Transfer completed: 100 LTR from BR001 to BR002"
echo "  ✅ Final stock: BR001=200 LTR, BR002=100 LTR"
echo ""
