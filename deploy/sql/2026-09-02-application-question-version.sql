-- Optimistic locking for application_question.
-- Two admins saving the same question used to overwrite each other silently; the loser got no signal.
-- Production uses ddl-auto=validate, so the app will not boot without this column.
-- Safe to re-run: adds the column only when it is missing.

SET @has_version = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'application_question'
      AND COLUMN_NAME = 'version'
);

SET @add_version = IF(
    @has_version = 0,
    'ALTER TABLE application_question ADD COLUMN version BIGINT NOT NULL DEFAULT 0',
    'SELECT 1'
);

PREPARE add_version_stmt FROM @add_version;
EXECUTE add_version_stmt;
DEALLOCATE PREPARE add_version_stmt;
