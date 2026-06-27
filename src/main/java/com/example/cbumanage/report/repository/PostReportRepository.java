package com.example.cbumanage.report.repository;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.report.entity.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    Optional<PostReport> findByPostId(Long postId);

    List<PostReport> findAllByGroupId(Long groupId);

    /*
    카테고리에 맞는 게시글, 연결된 그룹과 보고서를 left join하여 dto로 반환하는 코드 입니다
    PostDTO$PostReportPreviewDTO 는 인텔리제이에선 빨간줄이 뜨지만 실제로는 문제없이 작동 합니다
     */
    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,m.generation,
    r.isAccepted,

    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    ),
    g.category,
    r.date
    )
    from Post p
    join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join User m on m.userId = p.authorId
    where p.category = :category
    and p.isDeleted = false
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""",
    countQuery = """
    select count(p)
    from Post p
    join PostReport r on r.post = p
    where p.category =:category
    and p.isDeleted = false
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""")
    Page<PostDTO.PostReportPreviewDTO> findPostReportPreviews(Pageable pageable, @Param("category") int category,
                                                              @Param("startDate") LocalDateTime startDate,
                                                              @Param("endDate") LocalDateTime endDate);


    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,m.generation,
    r.isAccepted,

    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    ),
    g.category,
    r.date
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join User m on m.userId = p.authorId
    where p.category = :category and p.authorId = :userId
    and p.isDeleted = false
""",
            countQuery = """
    select count(p)
    from Post p
    where p.category =:category
    and p.isDeleted = false and p.authorId = :userId
""")
    Page<PostDTO.PostReportPreviewDTO> findMyPostReportPreviews(Pageable pageable, @Param("category")int category,Long userId);

    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,m.generation,
    r.isAccepted,

    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    ),
    g.category,
    r.date
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join User m on m.userId = p.authorId
    where p.category = :category and r.groupId = :groupId
    and p.isDeleted = false
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""",
            countQuery = """
    select count(p)
    from Post p
    left join PostReport r on r.post = p
    where p.category =:category
    and p.isDeleted = false and r.groupId = :groupId
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""")
    Page<PostDTO.PostReportPreviewDTO> findPostReportPreviewsByGroupId(Pageable pageable, @Param("category") int category, @Param("groupId") Long groupId,
                                                                       @Param("startDate") LocalDateTime startDate,
                                                                       @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,m.generation,
    r.isAccepted,

    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    ),
    g.category,
    r.date
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join User m on m.userId = p.authorId
    where p.category = :category
    and r.groupId in :groupIds
    and p.isDeleted = false
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""",
    countQuery = """
    select count(p)
    from Post p
    left join PostReport r on r.post = p
    where p.category = :category
    and r.groupId in :groupIds
    and p.isDeleted = false
    and (:startDate is null or r.date >= :startDate)
    and (:endDate is null or r.date <= :endDate)
""")
    Page<PostDTO.PostReportPreviewDTO> findPostReportPreviewsByGroupIds(Pageable pageable, @Param("category") int category,
                                                                        @Param("groupIds") Collection<Long> groupIds,
                                                                        @Param("startDate") LocalDateTime startDate,
                                                                        @Param("endDate") LocalDateTime endDate);

    @Query(
        value = """
            SELECT p.post_id
            FROM post p
            JOIN post_report r ON r.post_id = p.post_id
            JOIN user m ON m.user_id = p.author_id
            WHERE p.category = :category
              AND p.is_deleted = false
              AND (:startDate IS NULL OR r.`date` >= :startDate)
              AND (:endDate IS NULL OR r.`date` <= :endDate)
              AND CONCAT(p.title, ' ', m.name) REGEXP :pattern
            ORDER BY p.created_at DESC, p.post_id DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM post p
            JOIN post_report r ON r.post_id = p.post_id
            JOIN user m ON m.user_id = p.author_id
            WHERE p.category = :category
              AND p.is_deleted = false
              AND (:startDate IS NULL OR r.`date` >= :startDate)
              AND (:endDate IS NULL OR r.`date` <= :endDate)
              AND CONCAT(p.title, ' ', m.name) REGEXP :pattern
            """,
        nativeQuery = true
    )
    Page<Long> searchPostIdsByKeyword(
            @Param("category") int category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    @Query(
        value = """
            SELECT p.post_id
            FROM post p
            JOIN post_report r ON r.post_id = p.post_id
            JOIN user m ON m.user_id = p.author_id
            WHERE p.category = :category
              AND r.group_id IN :groupIds
              AND p.is_deleted = false
              AND (:startDate IS NULL OR r.`date` >= :startDate)
              AND (:endDate IS NULL OR r.`date` <= :endDate)
              AND CONCAT(p.title, ' ', m.name) REGEXP :pattern
            ORDER BY p.created_at DESC, p.post_id DESC
            """,
        countQuery = """
            SELECT COUNT(*)
            FROM post p
            JOIN post_report r ON r.post_id = p.post_id
            JOIN user m ON m.user_id = p.author_id
            WHERE p.category = :category
              AND r.group_id IN :groupIds
              AND p.is_deleted = false
              AND (:startDate IS NULL OR r.`date` >= :startDate)
              AND (:endDate IS NULL OR r.`date` <= :endDate)
              AND CONCAT(p.title, ' ', m.name) REGEXP :pattern
            """,
        nativeQuery = true
    )
    Page<Long> searchPostIdsByKeywordAndGroupIds(
            @Param("category") int category,
            @Param("groupIds") Collection<Long> groupIds,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("pattern") String pattern,
            Pageable pageable
    );

    @Query("""
        select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
        p.id,p.title,p.createdAt,p.authorId,m.name,m.generation,
        r.isAccepted,
        g.id,g.groupName, (
        select count(gm)
        from GroupMember gm
        where gm.group.id = g.id
        and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
        ),
        r.date
        )
        from Post p
        join PostReport r on r.post = p
        left join Group g on r.groupId = g.id
        left join User m on m.userId = p.authorId
        where p.id in :postIds
        """)
    List<PostDTO.PostReportPreviewDTO> findPreviewsByPostIds(@Param("postIds") List<Long> postIds);
}
