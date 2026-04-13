-- ============================================================
-- DB Risk Guard - Export Queries
-- ============================================================
-- Run these queries on your MySQL database and export results as CSV.
-- Drop the CSV files into this data/ folder, then run:
--   java -jar db-risk-guard.jar --sync
--
-- File naming:
--   tables*.csv       -> used by --sync for large-tables.yml
--   indexes*.csv      -> used by --sync for index-metadata.json
--   slow_queries*.csv -> used by --advise for optimization recommendations
-- ============================================================


-- ============================================================
-- 1. TABLES: save as tables.csv
-- ============================================================
-- Lists all tables with more than 1 million rows.
-- Used by --sync to generate/update large-tables.yml.

SELECT TABLE_NAME, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_ROWS > 1000000
ORDER BY TABLE_ROWS DESC;


-- ============================================================
-- 2. INDEXES: save as indexes.csv
-- ============================================================
-- Lists all indexes across all tables in the current database.
-- Used by --sync to generate/update index-metadata.json.

SELECT TABLE_NAME, INDEX_NAME, NON_UNIQUE, SEQ_IN_INDEX, COLUMN_NAME
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;


-- ============================================================
-- 3. SLOW QUERIES: save as slow_queries.csv (optional)
-- ============================================================
-- Lists queries slower than 700ms from performance_schema.
-- Used by --advise to produce optimization recommendations.
--
-- NOTE: performance_schema must be enabled on your MySQL server.
-- If you get errors, check: SHOW VARIABLES LIKE 'performance_schema';

SELECT DIGEST_TEXT,
       COUNT_STAR,
       AVG_TIMER_WAIT / 1000000000 AS AVG_MS,
       SUM_ROWS_EXAMINED,
       SUM_ROWS_SENT,
       FIRST_SEEN,
       LAST_SEEN
FROM performance_schema.events_statements_summary_by_digest
WHERE SCHEMA_NAME = DATABASE()
  AND AVG_TIMER_WAIT > 700000000
ORDER BY AVG_TIMER_WAIT DESC
LIMIT 100;
