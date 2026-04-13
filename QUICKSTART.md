# Material Management System - Quick Start Guide

## ✅ Build Status: SUCCESS

Your application has been successfully compiled! All 121 source files compiled with only minor Lombok warnings (non-critical).

---

## 🚀 How to Run

### 1. Make sure you have configured your database

Edit `src/main/resources/application.yml` with your database credentials:
```yaml
datasource:
  url: jdbc:mysql://localhost:3306/mms_db
  username: your_username
  password: your_password
```

Or set environment variables:
```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=mms_db
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

### 2. Create the database (if not already created)

```sql
mysql -u root -p
CREATE DATABASE mms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
EXIT;
```

### 3. Run the application

```bash
# Option 1: Maven (recommended for development)
mvn spring-boot:run

# Option 2: Run packaged JAR
mvn clean package -DskipTests
java -jar target/material-management-system-1.0.0.jar
```

### 4. Verify application started

The application will start on: **http://localhost:8080/mms**

Look for these log messages:
```
✅ Flyway migration V1__init_master_tables.sql completed
✅ Flyway migration V2__purchase_tables.sql completed
✅ Flyway migration V3__inward_tables.sql completed
✅ Flyway migration V4__outward_tables.sql completed
✅ Flyway migration V5__transfer_tables.sql completed
✅ Flyway migration V6__stock_tables.sql completed
✅ Started MaterialManagementSystemApplication in X seconds
```

### 5. Access Swagger Documentation

Once running, open your browser:
**http://localhost:8080/mms/swagger-ui.html**

You'll see all 50+ API endpoints documented with examples!

---

## 📡 Quick API Test

Test the application is working:

```bash
# Test GET endpoint (list branches - will be empty initially)
curl http://localhost:8080/mms/api/v1/master/branches

# Expected response:
# {
#   "success": true,
#   "message": "Branches retrieved successfully",
#   "data": [],
#   "timestamp": "2026-02-12T..."
# }
```

---

## 🗄️ Verify Database Tables

After first run, check database:

```bash
mysql -u root -p mms_db -e "SHOW TABLES;"
```

Expected 24 tables:
```
approval_rule_master
branch_master
branch_material_stock
grn_detail
grn_header
group_master
issue_detail
issue_fifo_consumption
issue_header
item_master
location_master
manufacturer_master
material_stock_ledger
po_detail
po_header
role_master
stock_transfer_detail
stock_transfer_header
sub_group_master
supplier_invoice
supplier_master
unit_master
user_role_map
flyway_schema_history
```

---

## 🧪 Test Complete Workflow

See `test-api.sh` script for a complete workflow test that:
1. Creates master data (branch, item, supplier, etc.)
2. Creates a GRN (goods receipt)
3. Creates an Issue (material outward with FIFO)
4. Creates an Inter-branch Transfer
5. Generates reports

Run it:
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 📚 Available Documentation

1. **SYSTEM_DOCUMENTATION.md** - Complete technical reference (94 pages)
2. **DEVELOPMENT_PLAN.md** - Step-by-step development guide
3. **CLAUDE.md** - Quick reference
4. **Swagger UI** - Interactive API documentation (when app is running)

---

## 🔧 Common Issues

### Issue: Port 8080 already in use
**Solution:** Change port in `application.yml`:
```yaml
server:
  port: 8081
```

### Issue: Database connection failed
**Solution:** Verify MySQL is running and credentials are correct:
```bash
mysql -u root -p -e "SELECT 'Connection OK' as status;"
```

### Issue: Flyway migration failed
**Solution:** Check `flyway_schema_history` table:
```sql
SELECT * FROM flyway_schema_history WHERE success = 0;
```
If needed, repair:
```bash
mvn flyway:repair
```

---

## 🎯 Next Steps

### Immediate (You can do now):
1. ✅ Build complete
2. ⏭️ **Configure your database** (add your DB credentials)
3. ⏭️ **Run the application** (`mvn spring-boot:run`)
4. ⏭️ **Test APIs** with Swagger UI or curl
5. ⏭️ **Create master data** (branches, items, suppliers)
6. ⏭️ **Test GRN workflow**

### Development (Ongoing):
1. Add unit tests (see DEVELOPMENT_PLAN.md Phase 4)
2. Add integration tests
3. Enhance Swagger documentation
4. Add authentication/authorization
5. Add Excel/PDF export for reports

---

## 📞 Need Help?

Check the documentation:
- **SYSTEM_DOCUMENTATION.md** - Complete system reference
- **DEVELOPMENT_PLAN.md** - Step-by-step guide
- **Swagger UI** - API documentation with examples

---

**Status:** ✅ Application compiled successfully and ready to run!
