#!/bin/bash
# MMS Complete Setup and Startup Script

set -e  # Exit on error

echo "=========================================="
echo "Material Management System - Full Setup"
echo "=========================================="
echo ""

# Get script directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Step 1: Load environment variables
echo "📋 Step 1: Loading environment variables..."
if [ -f .env ]; then
    export $(cat .env | grep -v '^#' | xargs)
    echo "✅ Environment variables loaded"
else
    echo "⚠️  .env file not found, using defaults from application.yml"
fi
echo ""

# Step 2: Check Java version
echo "☕ Step 2: Checking Java version..."
java -version
if [ $? -ne 0 ]; then
    echo "❌ Java not found! Please install Java 17 or later."
    exit 1
fi
echo "✅ Java is installed"
echo ""

# Step 3: Check MySQL
echo "🗄️  Step 3: Checking MySQL..."
if ! command -v mysql &> /dev/null; then
    echo "❌ MySQL not found! Please install MySQL 8.0 or later."
    exit 1
fi
echo "✅ MySQL is installed"
echo ""

# Step 4: Setup database
echo "🗄️  Step 4: Setting up database..."
chmod +x setup-database.sh
# Try to create database (will prompt for password if needed)
echo "Creating database 'mms_db'..."
mysql -u ${DB_USERNAME:-root} -p${DB_PASSWORD:-password} -e "CREATE DATABASE IF NOT EXISTS mms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null || {
    echo "⚠️  Automatic database creation failed. Please create manually:"
    echo "   mysql -u root -p"
    echo "   CREATE DATABASE mms_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
    echo ""
    read -p "Press Enter after creating the database manually, or Ctrl+C to exit..."
}
echo "✅ Database ready"
echo ""

# Step 5: Build application
echo "🔨 Step 5: Building application..."
echo "Running: mvn clean compile"
mvn clean compile
if [ $? -ne 0 ]; then
    echo "❌ Build failed!"
    exit 1
fi
echo "✅ Build successful"
echo ""

# Step 6: Run application
echo "🚀 Step 6: Starting Spring Boot application..."
echo ""
echo "=========================================="
echo "Application will start on:"
echo "  Base URL: http://localhost:8080/mms"
echo "  API Base: http://localhost:8080/mms/api/v1"
echo ""
echo "Flyway will automatically run migrations (V1-V6)"
echo "Press Ctrl+C to stop the application"
echo "=========================================="
echo ""

# Export environment variables for Maven
export DB_HOST=${DB_HOST:-localhost}
export DB_PORT=${DB_PORT:-3306}
export DB_NAME=${DB_NAME:-mms_db}
export DB_USERNAME=${DB_USERNAME:-root}
export DB_PASSWORD=${DB_PASSWORD:-password}

# Run Spring Boot
mvn spring-boot:run