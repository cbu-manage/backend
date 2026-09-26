-- Add post_freeboard.topic for the free board's 말머리 (일상/질문/잡담/홍보).
--
-- Why: the write screen has always offered 말머리 buttons but there was nowhere to store the
-- choice, so the list filter matched nothing. PostFreeboard now carries a `topic` field.
-- Production runs ddl-auto=validate, so the column has to exist before the app that ships
-- the entity starts.
--
-- Nullable on purpose: posts written before this change have no 말머리 and must stay readable.
-- Filtering by a topic simply excludes them.
--
-- Safe to re-run: the column is added only when absent. No rows are modified.

SET @stmt = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = DATABASE()
              AND table_name = 'post_freeboard'
              AND column_name = 'topic'
        ),
        'SELECT 1',
        'ALTER TABLE post_freeboard ADD COLUMN topic VARCHAR(20) NULL'
    )
);
PREPARE s FROM @stmt; EXECUTE s; DEALLOCATE PREPARE s;

SET @idx = (
    SELECT IF(
        EXISTS(
            SELECT 1 FROM information_schema.statistics
            WHERE table_schema = DATABASE()
              AND table_name = 'post_freeboard'
              AND index_name = 'idx_post_freeboard_topic'
        ),
        'SELECT 1',
        'CREATE INDEX idx_post_freeboard_topic ON post_freeboard (topic)'
    )
);
PREPARE s2 FROM @idx; EXECUTE s2; DEALLOCATE PREPARE s2;

-- Verify:
--   SELECT column_name, column_type, is_nullable FROM information_schema.columns
--   WHERE table_schema = DATABASE() AND table_name = 'post_freeboard' ORDER BY ordinal_position;
