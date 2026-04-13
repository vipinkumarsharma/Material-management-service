#!/bin/bash
# Create Initial Master Data for MMS

set -e

BASE_URL="http://localhost:8080/mms/api/v1"

echo "=========================================="
echo "Creating Master Data for MMS"
echo "=========================================="
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m'

print_step() {
    echo ""
    echo "${BLUE}$1${NC}"
    echo "----------------------------------------"
}

print_step "1. Creating Branches"

curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR001",
    "branchName": "Delhi Warehouse",
    "address": "Sector 18, Noida, Delhi NCR",
    "contactPerson": "Rajesh Kumar",
    "contactNumber": "9876543210",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR002",
    "branchName": "Mumbai Warehouse",
    "address": "Andheri East, Mumbai",
    "contactPerson": "Priya Sharma",
    "contactNumber": "9876543211",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "BR003",
    "branchName": "Bangalore Warehouse",
    "address": "Whitefield, Bangalore",
    "contactPerson": "Amit Patel",
    "contactNumber": "9876543212",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 3 branches${NC}"

print_step "2. Creating Product Groups"

curl -s -X POST "$BASE_URL/master/groups" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP01",
    "groupDesc": "Dairy Products",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/groups" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP02",
    "groupDesc": "Packaging Materials",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 2 product groups${NC}"

print_step "3. Creating Sub-Groups"

curl -s -X POST "$BASE_URL/master/groups/GRP01/sub-groups" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP01",
    "subGroupId": "SG01",
    "subGroupDesc": "Fresh Milk",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/groups/GRP01/sub-groups" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP01",
    "subGroupId": "SG02",
    "subGroupDesc": "Yogurt & Curd",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/groups/GRP02/sub-groups" \
  -H "Content-Type: application/json" \
  -d '{
    "groupId": "GRP02",
    "subGroupId": "SG01",
    "subGroupDesc": "Bottles & Containers",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 3 sub-groups${NC}"

print_step "4. Creating Units of Measurement"

curl -s -X POST "$BASE_URL/master/units" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "LTR",
    "unitDesc": "Litre",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/units" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "KG",
    "unitDesc": "Kilogram",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/units" \
  -H "Content-Type: application/json" \
  -d '{
    "unitId": "PCS",
    "unitDesc": "Pieces",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 3 units${NC}"

print_step "5. Creating Manufacturers"

curl -s -X POST "$BASE_URL/master/manufacturers" \
  -H "Content-Type: application/json" \
  -d '{
    "manufId": "MF001",
    "manufName": "Amul Dairy",
    "address": "Anand, Gujarat",
    "contactNumber": "9876543213",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/manufacturers" \
  -H "Content-Type: application/json" \
  -d '{
    "manufId": "MF002",
    "manufName": "Mother Dairy",
    "address": "New Delhi",
    "contactNumber": "9876543214",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 2 manufacturers${NC}"

print_step "6. Creating Suppliers"

curl -s -X POST "$BASE_URL/master/suppliers" \
  -H "Content-Type: application/json" \
  -d '{
    "suppId": "SUP001",
    "suppName": "Fresh Foods Supplier",
    "address": "Delhi",
    "contactPerson": "Amit Verma",
    "contactNumber": "9876543215",
    "gstNo": "27AAAAA1234A1Z5",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/suppliers" \
  -H "Content-Type: application/json" \
  -d '{
    "suppId": "SUP002",
    "suppName": "Quality Packaging Co",
    "address": "Mumbai",
    "contactPerson": "Sneha Desai",
    "contactNumber": "9876543216",
    "gstNo": "27BBBBB5678B2Z6",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 2 suppliers${NC}"

print_step "7. Creating Storage Locations (Warehouse Hierarchy)"

# Delhi Warehouse Locations
curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "WH-DL-01",
    "locationName": "Delhi Main Warehouse",
    "branchId": "BR001",
    "locationType": "WAREHOUSE",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "SHELF-DL-A",
    "locationName": "Shelf A - Cold Storage",
    "branchId": "BR001",
    "parentLocationId": "WH-DL-01",
    "locationType": "SHELF",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "BIN-DL-A1",
    "locationName": "Bin A1",
    "branchId": "BR001",
    "parentLocationId": "SHELF-DL-A",
    "locationType": "BIN",
    "capacity": 1000,
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "BIN-DL-A2",
    "locationName": "Bin A2",
    "branchId": "BR001",
    "parentLocationId": "SHELF-DL-A",
    "locationType": "BIN",
    "capacity": 1000,
    "active": true
  }' | jq -r '.message'

# Mumbai Warehouse Locations
curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "WH-MB-01",
    "locationName": "Mumbai Main Warehouse",
    "branchId": "BR002",
    "locationType": "WAREHOUSE",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/locations" \
  -H "Content-Type: application/json" \
  -d '{
    "locationId": "BIN-MB-B1",
    "locationName": "Bin B1",
    "branchId": "BR002",
    "parentLocationId": "WH-MB-01",
    "locationType": "BIN",
    "capacity": 800,
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 6 storage locations${NC}"

print_step "8. Creating Items (Products)"

curl -s -X POST "$BASE_URL/master/items" \
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
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/items" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "ITM002",
    "itemName": "Toned Milk",
    "itemDesc": "Toned milk 500ml pack",
    "groupId": "GRP01",
    "subGroupId": "SG01",
    "manufId": "MF002",
    "unitId": "LTR",
    "hsnCode": "0401",
    "gstPct": 5.0,
    "costPrice": 25.00,
    "mrp": 35.00,
    "reorderLevel": 150,
    "maxStockLevel": 1500,
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/items" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "ITM003",
    "itemName": "Fresh Curd",
    "itemDesc": "Fresh curd 400g pack",
    "groupId": "GRP01",
    "subGroupId": "SG02",
    "manufId": "MF001",
    "unitId": "KG",
    "hsnCode": "0403",
    "gstPct": 5.0,
    "costPrice": 35.00,
    "mrp": 50.00,
    "reorderLevel": 80,
    "maxStockLevel": 500,
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/items" \
  -H "Content-Type: application/json" \
  -d '{
    "itemId": "ITM004",
    "itemName": "Plastic Bottles 1L",
    "itemDesc": "1 Litre plastic bottles for milk",
    "groupId": "GRP02",
    "subGroupId": "SG01",
    "manufId": "MF001",
    "unitId": "PCS",
    "hsnCode": "3923",
    "gstPct": 18.0,
    "costPrice": 8.00,
    "mrp": 12.00,
    "reorderLevel": 500,
    "maxStockLevel": 5000,
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 4 items${NC}"

print_step "9. Creating Roles"

curl -s -X POST "$BASE_URL/master/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "PM",
    "roleName": "Purchase Manager",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "WM",
    "roleName": "Warehouse Manager",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/roles" \
  -H "Content-Type: application/json" \
  -d '{
    "roleId": "FH",
    "roleName": "Finance Head",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 3 roles${NC}"

print_step "10. Creating Approval Rules"

curl -s -X POST "$BASE_URL/master/approval-rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "APR001",
    "txnType": "GRN",
    "conditionType": "PRICE_VARIANCE",
    "minValue": 5.0,
    "maxValue": 10.0,
    "requiredRole": "PM",
    "active": true
  }' | jq -r '.message'

curl -s -X POST "$BASE_URL/master/approval-rules" \
  -H "Content-Type: application/json" \
  -d '{
    "ruleId": "APR002",
    "txnType": "GRN",
    "conditionType": "PRICE_VARIANCE",
    "minValue": 10.0,
    "maxValue": 999.0,
    "requiredRole": "FH",
    "active": true
  }' | jq -r '.message'

echo "${GREEN}✅ Created 2 approval rules${NC}"

echo ""
echo "${GREEN}=========================================="
echo "Master Data Creation Complete!"
echo "==========================================${NC}"
echo ""
echo "Summary:"
echo "  ✅ 3 Branches"
echo "  ✅ 2 Product Groups"
echo "  ✅ 3 Sub-Groups"
echo "  ✅ 3 Units of Measurement"
echo "  ✅ 2 Manufacturers"
echo "  ✅ 2 Suppliers"
echo "  ✅ 6 Storage Locations (with hierarchy)"
echo "  ✅ 4 Items (Products)"
echo "  ✅ 3 Roles"
echo "  ✅ 2 Approval Rules"
echo ""
echo "Next Steps:"
echo "  1. View all data in Swagger UI: http://localhost:8080/mms/swagger-ui.html"
echo "  2. Create a GRN (Goods Receipt)"
echo "  3. Create an Issue (Material Outward)"
echo "  4. Test inter-branch transfer"
echo "  5. Generate reports"
echo ""
