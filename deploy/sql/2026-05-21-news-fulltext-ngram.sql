-- News keyword search index.
-- Before applying, verify MySQL ngram parser is available and ngram_token_size=2.
-- This script tolerates ddl-auto=update creating news.search_text before the migration runs.

SET @has_news_search_text = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'news'
      AND COLUMN_NAME = 'search_text'
);

SET @add_news_search_text = IF(
    @has_news_search_text = 0,
    'ALTER TABLE news ADD COLUMN search_text TEXT NULL',
    'SELECT 1'
);

PREPARE add_news_search_text_stmt FROM @add_news_search_text;
EXECUTE add_news_search_text_stmt;
DEALLOCATE PREPARE add_news_search_text_stmt;

UPDATE news n
JOIN post p ON p.post_id = n.post_id
SET n.search_text = TRIM(
        REGEXP_REPLACE(
            REGEXP_REPLACE(CONCAT_WS(' ', p.title, p.content), '<[^>]*>', ' '),
            '[[:space:]]+',
            ' '
        )
    )
WHERE n.search_text IS NULL;

ALTER TABLE news
    MODIFY COLUMN search_text TEXT NOT NULL;

SET @has_news_search_index = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'news'
      AND INDEX_NAME = 'ft_news_search_text_ngram'
);

SET @add_news_search_index = IF(
    @has_news_search_index = 0,
    'ALTER TABLE news ADD FULLTEXT INDEX ft_news_search_text_ngram (search_text) WITH PARSER ngram',
    'SELECT 1'
);

PREPARE add_news_search_index_stmt FROM @add_news_search_index;
EXECUTE add_news_search_index_stmt;
DEALLOCATE PREPARE add_news_search_index_stmt;
