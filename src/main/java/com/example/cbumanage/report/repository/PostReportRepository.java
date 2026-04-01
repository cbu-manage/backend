package com.example.cbumanage.report.repository;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.report.entity.PostReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostReportRepository extends JpaRepository<PostReport, Long> {
    PostReport findByPostId(Long postId);

    /*
    카테고리에 맞는 게시글, 연결된 그룹과 보고서를 left join하여 dto로 반환하는 코드 입니다
    PostDTO$PostReportPreviewDTO 는 인텔리제이에선 빨간줄이 뜨지만 실제로는 문제없이 작동 합니다
     */
    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,
    r.type,r.isAccepted,
    
    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    )
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join CbuMember m on m.cbuMemberId = p.authorId
    where p.category = :category
    and p.isDeleted = false
""",
    countQuery = """
    select count(p)
    from Post p
    where p.category =:category
    and p.isDeleted = false
""")
    Page<PostDTO.PostReportPreviewDTO> findPostReportPreviews(Pageable pageable, @Param("category")int category);


    @Query(value = """
    select new com.example.cbumanage.post.dto.PostDTO$PostReportPreviewDTO(
    p.id,p.title,p.createdAt,p.authorId,m.name,
    r.type,r.isAccepted,
    
    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    )
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join CbuMember m on m.cbuMemberId = p.authorId
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
    p.id,p.title,p.createdAt,p.authorId,m.name,
    r.type,r.isAccepted,

    g.id,g.groupName, (
    select count(gm)
    from GroupMember gm
    where gm.group.id = g.id
    and gm.groupMemberStatus=com.example.cbumanage.group.entity.enums.GroupMemberStatus.ACTIVE
    )
    )
    from Post p
    left join PostReport r on r.post = p
    left join Group g on r.groupId = g.id
    left join CbuMember m on m.cbuMemberId = p.authorId
    where p.category = :category and r.groupId = :groupId
    and p.isDeleted = false
""",
            countQuery = """
    select count(p)
    from Post p
    left join PostReport r on r.post = p
    where p.category =:category
    and p.isDeleted = false and r.groupId = :groupId
""")
    Page<PostDTO.PostReportPreviewDTO> findPostReportPreviewsByGroupId(Pageable pageable, @Param("category") int category, @Param("groupId") Long groupId);
}
