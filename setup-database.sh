#!/bin/bash
# MMS Database Setup Script

echo "=========================================="
echo "Material Management System - DB Setup"
echo "=========================================="

# Database configuration
DB_NAME="mms_db"
DB_USER="root"
DB_PASSWORD="password"

echo ""
echo "Creating database: $DB_NAME"
echo ""

# Create database
mysql -u $DB_USER -p$DB_PASSWORD <<EOF
-- Drop database if exists (for clean setup)
DROP DATABASE IF EXISTS $DB_NAME;

-- Create database
CREATE DATABASE $DB_NAME CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify database created
SHOW DATABASES LIKE '$DB_NAME';

-- Select database
USE $DB_NAME;

SELECT 'Database created successfully!' as Status;
EOF

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Database setup completed successfully!"
    echo "Database name: $DB_NAME"
    echo ""
else
    echo ""
    echo "❌ Database setup failed!"
    echo "Please check your MySQL credentials and try again."
    echo ""
    exit 1
fi