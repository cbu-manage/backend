-- Widen comment.content to 1000 characters.
--
-- Why: the entity declared `private String content;` with no length, so Hibernate created
-- VARCHAR(255). Posting a 256-character comment raised a DataIntegrityViolationException, and
-- GlobalExceptionHandler maps every DataIntegrityViolation to DUPLICATE_RESOURCE, so the user
-- saw 409 "중복 리소스" for a comment that was simply too long. The web form meanwhile
-- advertises a 1,000-character limit.
--
-- The entity now carries @Column(length = 1000) and the request DTOs reject anything longer
-- with 400 before it reaches the database. Production runs ddl-auto=validate, so the column
-- has to be widened here or the application will fail to start against the old schema.
--
-- Safe to re-run: widening an already-wide column is a no-op, and the guard below skips it.
-- No data loss: this only grows the column.

-- 1) Look first. Expect character_maximum_length = 255 before, 1000 after.
--    SELECT column_name, data_type, character_maximum_length, is_nullable
--    FROM information_schema.columns
--    WHERE table_schema = DATABASE() AND table_name = 'comment' AND column_name = 'content';

-- 2) Widen only when it is still narrower than 1000.
SET @needs_widening := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'comment'
      AND column_name = 'content'
      AND (character_maximum_length IS NULL OR character_maximum_length < 1000)
);

SET @ddl := IF(@needs_widening > 0,
    'ALTER TABLE comment MODIFY COLUMN content VARCHAR(1000)',
    'SELECT ''comment.content is already 1000 or wider — nothing to do''');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3) Verify with the SELECT in step 1.
