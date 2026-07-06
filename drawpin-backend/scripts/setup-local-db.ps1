#!/usr/bin/env pwsh
# =============================================================================
# DrawPin 2.0 — Local PostgreSQL Database Setup
# =============================================================================
# Run this script ONCE to provision the local development database.
# It creates the 'drawpin' role and 'drawpin' database.
#
# Usage:
#   .\scripts\setup-local-db.ps1
#
# If prompted for the postgres superuser password, enter the password you
# set when you installed PostgreSQL. For a default installation this is
# usually the password you typed during the PostgreSQL installer wizard.
# =============================================================================

$PG_BIN    = "C:\Program Files\PostgreSQL\18\bin"
$PSQL      = "$PG_BIN\psql.exe"
$PG_SUPERUSER = "postgres"

# DrawPin dev credentials (must match application.yml defaults)
$DB_NAME   = "drawpin"
$DB_USER   = "drawpin"
$DB_PASS   = "drawpin_secret"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  DrawPin 2.0 — Database Setup Script  " -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "This script will create:" -ForegroundColor Yellow
Write-Host "  Role     : $DB_USER" -ForegroundColor Yellow
Write-Host "  Database : $DB_NAME" -ForegroundColor Yellow
Write-Host "  Password : $DB_PASS" -ForegroundColor Yellow
Write-Host ""
Write-Host "You will be prompted for the 'postgres' superuser password." -ForegroundColor Magenta
Write-Host "(This is the password you set during PostgreSQL installation.)" -ForegroundColor Gray
Write-Host ""

# ─────────────────────────────────────────────────────────────────────────────
# Step 1: Create the role
# ─────────────────────────────────────────────────────────────────────────────
Write-Host "[1/3] Creating role '$DB_USER'..." -ForegroundColor Cyan

$createRoleSQL = @"
DO `$`$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '$DB_USER') THEN
      CREATE ROLE $DB_USER WITH LOGIN PASSWORD '$DB_PASS';
      RAISE NOTICE 'Role $DB_USER created.';
   ELSE
      ALTER ROLE $DB_USER WITH PASSWORD '$DB_PASS';
      RAISE NOTICE 'Role $DB_USER already exists - password updated.';
   END IF;
END
`$`$;
"@

& $PSQL -U $PG_SUPERUSER -c $createRoleSQL

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "ERROR: Failed to create role. Please check your postgres password." -ForegroundColor Red
    exit 1
}

# ─────────────────────────────────────────────────────────────────────────────
# Step 2: Create the database
# ─────────────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[2/3] Creating database '$DB_NAME'..." -ForegroundColor Cyan

# Check if database already exists
$dbExists = & $PSQL -U $PG_SUPERUSER -tAc "SELECT 1 FROM pg_database WHERE datname='$DB_NAME'" 2>$null
if ($dbExists -eq "1") {
    Write-Host "  Database '$DB_NAME' already exists — skipping creation." -ForegroundColor Yellow
} else {
    & $PSQL -U $PG_SUPERUSER -c "CREATE DATABASE $DB_NAME OWNER $DB_USER ENCODING 'UTF8';"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "ERROR: Failed to create database." -ForegroundColor Red
        exit 1
    }
}

# ─────────────────────────────────────────────────────────────────────────────
# Step 3: Grant privileges
# ─────────────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "[3/3] Granting privileges..." -ForegroundColor Cyan

& $PSQL -U $PG_SUPERUSER -c "GRANT ALL PRIVILEGES ON DATABASE $DB_NAME TO $DB_USER;"
& $PSQL -U $PG_SUPERUSER -d $DB_NAME -c "GRANT ALL ON SCHEMA public TO $DB_USER;"

# ─────────────────────────────────────────────────────────────────────────────
# Done
# ─────────────────────────────────────────────────────────────────────────────
Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  ✅ Database setup complete!           " -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Database : $DB_NAME" -ForegroundColor Green
Write-Host "  User     : $DB_USER" -ForegroundColor Green
Write-Host "  Password : $DB_PASS" -ForegroundColor Green
Write-Host ""
Write-Host "Next step: run the application" -ForegroundColor Cyan
Write-Host "  cd drawpin-backend" -ForegroundColor White
Write-Host "  mvn spring-boot:run" -ForegroundColor White
Write-Host ""
