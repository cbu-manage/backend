-- News attachment storage.
-- Production uses ddl-auto=none, so apply this script before deploying News attachments.
-- This script tolerates ddl-auto=update having already created news_attachment in dev.
-- Column types mirror the JPA mapping so dev (ddl-auto=update) and prod (ddl-auto=none) converge.

CREATE TABLE IF NOT EXISTS news_attachment (
    attachment_id      BIGINT       NOT NULL AUTO_INCREMENT,
    news_id            BIGINT       NOT NULL,
    s3_key             VARCHAR(512) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    content_type       VARCHAR(150) NULL,
    file_size          BIGINT       NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    PRIMARY KEY (attachment_id),
    KEY idx_news_attachment_news (news_id),
    CONSTRAINT fk_news_attachment_news FOREIGN KEY (news_id) REFERENCES news (news_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
