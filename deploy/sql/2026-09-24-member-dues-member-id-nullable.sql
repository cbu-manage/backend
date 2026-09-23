-- member_dues.member_id 는 Dues 엔티티가 매핑하지 않는 컬럼인데 NOT NULL 이고 기본값이 없다.
-- post_report.is_accepted 와 같은 상태로, 회비 저장을 연결하는 순간 INSERT 가 전부 실패한다.
-- 지금은 DuesService 를 호출하는 곳이 없어 드러나지 않을 뿐이다.
ALTER TABLE member_dues
    MODIFY COLUMN member_id BIGINT NULL;
