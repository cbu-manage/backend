-- Newsletter sub-classification (WEEKLY / SPECIAL / NOTICE).
-- Production uses ddl-auto=none, so apply this script before deploying newsletter sub-categories.
-- This script tolerates ddl-auto=update having already added news.newsletter_type in dev.
-- Column type mirrors the JPA mapping (@Enumerated(STRING), length 20).

SET @has_newsletter_type = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'news'
      AND COLUMN_NAME = 'newsletter_type'
);

SET @add_newsletter_type = IF(
    @has_newsletter_type = 0,
    'ALTER TABLE news ADD COLUMN newsletter_type VARCHAR(20) NULL',
    'SELECT 1'
);

PREPARE add_newsletter_type_stmt FROM @add_newsletter_type;
EXECUTE add_newsletter_type_stmt;
DEALLOCATE PREPARE add_newsletter_type_stmt;

SET @has_newsletter_type_index = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'news'
      AND INDEX_NAME = 'idx_news_newsletter_type'
);

SET @add_newsletter_type_index = IF(
    @has_newsletter_type_index = 0,
    'ALTER TABLE news ADD INDEX idx_news_newsletter_type (newsletter_type)',
    'SELECT 1'
);

PREPARE add_newsletter_type_index_stmt FROM @add_newsletter_type_index;
EXECUTE add_newsletter_type_index_stmt;
DEALLOCATE PREPARE add_newsletter_type_index_stmt;
