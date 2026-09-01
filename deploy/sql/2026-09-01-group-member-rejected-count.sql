-- Group member reject count, used to block re-application after repeated rejections.
-- Production uses ddl-auto=none, so apply this script before deploying the re-application limit.
-- This script tolerates ddl-auto=update having already added group_member.rejected_count in dev.
-- Column type mirrors the JPA mapping (int, NOT NULL, default 0).

SET @has_rejected_count = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'group_member'
      AND COLUMN_NAME = 'rejected_count'
);

SET @add_rejected_count = IF(
    @has_rejected_count = 0,
    'ALTER TABLE group_member ADD COLUMN rejected_count INT NOT NULL DEFAULT 0',
    'SELECT 1'
);

PREPARE add_rejected_count_stmt FROM @add_rejected_count;
EXECUTE add_rejected_count_stmt;
DEALLOCATE PREPARE add_rejected_count_stmt;
