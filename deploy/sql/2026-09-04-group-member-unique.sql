-- (group_id, user_id) uniqueness for group_member.
--
-- Why: GroupMemberRepository.findByGroupIdAndUserUserId returns a single entity, not Optional/List.
-- Applying twice (double-click, retry) inserts a second PENDING row because the service does a
-- check-then-write with no constraint behind it. From that point on every call touching that
-- (user, group) pair throws NonUniqueResultException -> HTTP 500, including hasAppliedToGroup(),
-- which the study/project DETAIL page calls. So the page stops opening and only a manual DB fix
-- brings it back.
--
-- Safe to re-run: creates the index only when it is missing.
-- NOT safe to run blindly: if duplicates already exist the CREATE fails. Check first with the
-- SELECT below, resolve the rows, then run this script.

-- 1) Look before you leap. Expect zero rows.
--    SELECT group_id, user_id, COUNT(*) c, GROUP_CONCAT(group_member_id) ids
--    FROM group_member
--    GROUP BY group_id, user_id
--    HAVING c > 1;
--
--    If rows come back, keep the earliest id per pair and delete the rest, e.g.
--    DELETE gm FROM group_member gm
--    JOIN (
--        SELECT group_id, user_id, MIN(group_member_id) keep_id
--        FROM group_member GROUP BY group_id, user_id HAVING COUNT(*) > 1
--    ) d ON gm.group_id = d.group_id AND gm.user_id = d.user_id AND gm.group_member_id > d.keep_id;

-- 2) Add the constraint.
SET @has_unique = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'group_member'
      AND INDEX_NAME = 'uk_group_member_group_user'
);

SET @add_unique = IF(
    @has_unique = 0,
    'ALTER TABLE group_member ADD CONSTRAINT uk_group_member_group_user UNIQUE (group_id, user_id)',
    'SELECT 1'
);

PREPARE add_unique_stmt FROM @add_unique;
EXECUTE add_unique_stmt;
DEALLOCATE PREPARE add_unique_stmt;

-- After this, a duplicate insert raises DataIntegrityViolationException, which
-- GlobalExceptionHandler maps to 409 E-COMMON-0003 instead of corrupting the pair.
