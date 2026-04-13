#!/bin/bash
# Insert Beejapuri Dairy branches via REST API

set -e

BASE_URL="http://localhost:8080/mms/api/v1"

echo "Inserting Beejapuri Dairy branches..."
echo ""

# Branch 1: TKRIGGN - Tikri
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "TKRIGGN",
    "branchName": "Beejapuri Dairy Pvt Ltd-Tikri",
    "address1": "Plot No- 15/1/2/2, Village - Tikri , Sec -48 ,Gurgaon-122001",
    "gstNo": "06AAFCB1753M1ZL",
    "pincode": "122001",
    "active": true
  }' | jq -r '.message'

# Branch 2: KHTLGGN - Khatola
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "KHTLGGN",
    "branchName": "Beejapuri Dairy Pvt Ltd-Khtl",
    "address1": "Warehouse Near By JBM Company,Village Khatola Sector-74,Gurgaon(HR)",
    "gstNo": "06AAFCB1753M1ZL",
    "pincode": "122004",
    "active": true
  }' | jq -r '.message'

# Branch 3: JHAJRGGN - Jhajjar
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "JHAJRGGN",
    "branchName": "Beejapuri Dairy Pvt Ltd-Jhjr",
    "address1": "Khewat / Khata no. 297/345, Mustil / Killa No. 3//8/1 (3-7), 13/2 (4-4), 18/1 (7-7), 22/2 (0-2), 23 (8-0) Village Nangla, Tehsil Badli, District Jhajjar",
    "gstNo": "06AAFCB1753M1ZL",
    "pincode": "124103",
    "active": true
  }' | jq -r '.message'

# Branch 4: PUNE
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "PUNE",
    "branchName": "Beejapuri Dairy Pvt Ltd-Pune",
    "address1": "Gate No 302,Vill : Kharabwadi, Tal. Khed, Chakan Industrial Area, Pune",
    "gstNo": "27AAFCB1753M1ZH",
    "pincode": "410501",
    "active": true
  }' | jq -r '.message'

# Branch 5: CHITTOOR
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "CHITTOOR",
    "branchName": "Beejapuri Dairy Pvt Ltd-Chittr",
    "address1": "502-3A 502-2 502-1A 502-3 Gandharamakulapalle vill. Kongatam post, Venkatagirikota Mandal, chittoor-District(Andhra Pradesh)",
    "gstNo": "37AAFCB1753M1ZG",
    "pincode": "517424",
    "active": true
  }' | jq -r '.message'

# Branch 6: HYDERABAD
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "HYDERABAD",
    "branchName": "Beejapuri Dairy Pvt Ltd-Hyd",
    "address1": "Sy.No.339, IDA, PH-V , Chinnakanjarla(Village), Patancheru(Mandal), Sangareddy District – 502319 Telangana",
    "gstNo": "36AAFCB1753M1ZI",
    "pincode": "502319",
    "active": true
  }' | jq -r '.message'

# Branch 7: KOLKATA
curl -s -X POST "$BASE_URL/master/branches" \
  -H "Content-Type: application/json" \
  -d '{
    "branchId": "KOLKATA",
    "branchName": "Beejapuri Dairy Pvt Ltd-Klkta",
    "address1": "Plot No. 36,60 and 61, Mouza belumilki, JI No. 11, PS-Serampore, Belumilki Hooghly, West Bengal",
    "gstNo": "19AAFCB1753M1ZE",
    "pincode": "712223",
    "active": true
  }' | jq -r '.message'

echo ""
echo "✅ All 7 Beejapuri Dairy branches inserted successfully!"
echo ""
echo "Verify with:"
echo "  curl http://localhost:8080/mms/api/v1/master/branches | jq"
echo ""
