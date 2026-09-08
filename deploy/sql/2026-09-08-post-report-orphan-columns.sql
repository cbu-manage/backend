-- 보고서 작성이 유효한 입력에도 500 으로 실패하는 것을 고친다. (2026-07-01 이후 신규 보고서 0건)
--
-- 경위: PostReport 엔티티에 `private boolean isAccepted = false;` 가 있었다. 원시형 boolean 이라
-- Hibernate 가 컬럼을 NOT NULL 로 만들었고 DEFAULT 는 붙지 않았다. 2026-06-30 커밋 2bb2d6c 가
-- 이 필드를 엔티티에서 제거했지만 ddl-auto 는 컬럼을 지우지 않는다. 그 뒤로 INSERT 는 이 컬럼을
-- 빼고 나가고, MySQL strict 모드는 "Field 'is_accepted' doesn't have a default value"(1364) 로
-- 거절한다. 이 예외를 잡는 곳이 없어 원인이 500 으로만 보였다.
-- 같은 시기에 함께 빠진 type(@Enumerated, nullable)·report_file(String, nullable) 은 NULL 을
-- 허용하므로 INSERT 를 막지 않는다.
--
-- 이 스크립트는 스스로 진단한다. 실행하면 먼저 현재 스키마와 판정을 출력하고,
-- is_accepted 가 **실제로 NOT NULL + DEFAULT 없음** 일 때만 기본값을 붙인다.
-- 전제가 틀렸다면(컬럼이 없거나, nullable 이거나, 이미 DEFAULT 가 있으면) 아무것도 바꾸지 않는다.
-- 컬럼을 지우지 않으므로 되돌리기 쉽고 기존 승인 이력도 남는다. 컬럼 자체를 걷어내는 것은
-- 값을 읽는 곳이 없는지 확인한 뒤 별건으로 한다.
--
-- 재실행 안전. 적용 후 같은 진단을 한 번 더 출력하므로 결과를 눈으로 확인할 수 있다.

-- 1) 적용 전 진단 ------------------------------------------------------------
SELECT '=== post_report 현재 컬럼 ===' AS diagnosis;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'post_report'
ORDER BY ORDINAL_POSITION;

-- 엔티티에 없는데 NOT NULL + DEFAULT 없음 = INSERT 를 막는 고아 컬럼.
-- is_accepted 말고 다른 이름이 여기 나오면 그 컬럼도 같은 이유로 막고 있으니 함께 처리해야 한다.
SELECT '=== INSERT 를 막는 고아 컬럼 (없으면 원인이 다른 데 있음) ===' AS diagnosis;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT, EXTRA
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'post_report'
  AND IS_NULLABLE = 'NO'
  AND COLUMN_DEFAULT IS NULL
  AND EXTRA NOT LIKE '%auto_increment%'
  AND COLUMN_NAME NOT IN (
      -- 현재 엔티티가 값을 채워 넣는 컬럼들
      'post_report_id', 'post_id', 'group_id', 'date',
      'location', 'report_image', 'reflection', 'next_plan'
  )
ORDER BY ORDINAL_POSITION;

-- 2) 조건이 맞을 때만 기본값을 붙인다 ------------------------------------------
SET @col_type = (
    SELECT DATA_TYPE
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'post_report'
      AND COLUMN_NAME = 'is_accepted'
      AND IS_NULLABLE = 'NO'
      AND COLUMN_DEFAULT IS NULL
);

SET @set_default = IF(
    @col_type IS NULL,
    'SELECT ''is_accepted 가 조건에 맞지 않아 건너뜀 (없거나, nullable 이거나, 이미 DEFAULT 있음)'' AS result',
    CONCAT('ALTER TABLE post_report ALTER COLUMN is_accepted SET DEFAULT ',
           IF(@col_type = 'bit', 'b''0''', '0'))
);

SELECT @set_default AS `실행할 문장`;

PREPARE set_default_stmt FROM @set_default;
EXECUTE set_default_stmt;
DEALLOCATE PREPARE set_default_stmt;

-- 3) 적용 후 확인 -------------------------------------------------------------
SELECT '=== 적용 후 is_accepted ===' AS diagnosis;

SELECT COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_DEFAULT
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'post_report'
  AND COLUMN_NAME = 'is_accepted';

-- 되돌리기: ALTER TABLE post_report ALTER COLUMN is_accepted DROP DEFAULT;
