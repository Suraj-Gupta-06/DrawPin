-- DrawPin 2.0 — PostgreSQL Database Setup Script
-- Run once as the 'postgres' superuser to provision the local dev database.
-- Usage: psql -U postgres -f setup-db.sql

-- ─────────────────────────────────────────────────────────────────────────────
-- 1. Create role (skip if already exists)
-- ─────────────────────────────────────────────────────────────────────────────
DO $$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'drawpin') THEN
      CREATE ROLE drawpin WITH LOGIN PASSWORD 'drawpin_secret';
      RAISE NOTICE 'Role ''drawpin'' created.';
   ELSE
      ALTER ROLE drawpin WITH PASSWORD 'drawpin_secret';
      RAISE NOTICE 'Role ''drawpin'' already exists — password updated.';
   END IF;
END
$$;

-- ─────────────────────────────────────────────────────────────────────────────
-- 2. Create database (skip if already exists)
-- ─────────────────────────────────────────────────────────────────────────────
SELECT 'CREATE DATABASE drawpin OWNER drawpin ENCODING ''UTF8'' LC_COLLATE ''en_US.UTF-8'' LC_CTYPE ''en_US.UTF-8'' TEMPLATE template0'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'drawpin')\gexec

-- ─────────────────────────────────────────────────────────────────────────────
-- 3. Grant all privileges
-- ─────────────────────────────────────────────────────────────────────────────
GRANT ALL PRIVILEGES ON DATABASE drawpin TO drawpin;

\echo ''
\echo '✅ Database setup complete!'
\echo '   Database : drawpin'
\echo '   User     : drawpin'
\echo '   Password : drawpin_secret'
\echo ''
\echo 'You can now run: mvn spring-boot:run'
