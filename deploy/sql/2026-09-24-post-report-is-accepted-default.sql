-- post_report.is_accepted 는 엔티티가 매핑하지 않는 컬럼인데 NOT NULL 이고 기본값이 없다.
-- 그래서 보고서 저장 INSERT 에 이 컬럼이 빠지고, MySQL strict 모드가 전부 거부해 500 이 났다.
-- ddl-auto=validate 는 엔티티에 있는 컬럼만 확인해서 이 상태를 잡지 못한다.
ALTER TABLE post_report
    MODIFY COLUMN is_accepted BIT(1) NOT NULL DEFAULT b'0';
