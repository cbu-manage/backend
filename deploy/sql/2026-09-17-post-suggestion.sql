-- Create post_suggestion for the anonymous suggestion board (건의 게시판 / 대나무숲).
--
-- Why: PostSuggestion is a new entity (category 9 in `post`). Production runs ddl-auto=validate,
-- so the table has to exist before the application that ships the entity can start.
-- Column names/types mirror the entity exactly: suggestion_type/status are @Enumerated(STRING)
-- VARCHAR(20), resolved_at/pinned_at are nullable DATETIME(6), is_pinned is a NOT NULL boolean
-- (Hibernate maps boolean to BIT(1) on MySQL), post_id is unique (one row per post).
--
-- Safe to re-run: CREATE TABLE IF NOT EXISTS. No data is touched.

CREATE TABLE IF NOT EXISTS post_suggestion (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    post_id          BIGINT       NOT NULL,
    suggestion_type  VARCHAR(20)  NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    resolved_at      DATETIME(6)  NULL,
    is_pinned        BIT(1)       NOT NULL DEFAULT b'0',
    pinned_at        DATETIME(6)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_post_suggestion_post (post_id),
    KEY idx_post_suggestion_type (suggestion_type),
    KEY idx_post_suggestion_status (status),
    CONSTRAINT fk_post_suggestion_post FOREIGN KEY (post_id) REFERENCES post (post_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- Verify:
--   SELECT column_name, column_type, is_nullable FROM information_schema.columns
--   WHERE table_schema = DATABASE() AND table_name = 'post_suggestion' ORDER BY ordinal_position;
