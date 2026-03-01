package com.example.cbumanage.repository;

import com.example.cbumanage.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    //부모댓글이 없는댓글(답글이 아닌 댓글)만 찾아서 선착순으로 정렬합니다
//    List<Comment> findByPostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    /*
   댓글 목록을 불러오는 메소드입니다. 답글이 달려있지 않고, 삭제된 댓글일 경우 가져오지 않으며,
   답글이 달려있는 댓글이 삭제될경우, 일단 가져 온 후 mapper에서 삭제된 댓글 표시를 합니다
     */
    @Query("""
    select c
    from Comment c
    where c.post.id = :postId
      and c.parentComment is null
      and (
            c.isDeleted = false
            or exists (
                select 1 from Comment r
                where r.parentComment = c
            )
          )
    order by c.createdAt asc
""")
    List<Comment> findRoots(Long postId);

    // Problem 댓글 기능을 위해 새로 추가하는 메소드
    @Query("""
    select c
    from Comment c
    where c.problem.problemId = :problemId
      and c.parentComment is null
      and (
            c.isDeleted = false
            or exists (
                select 1 from Comment r
                where r.parentComment = c
            )
          )
    order by c.createdAt asc
""")
    List<Comment> findRootsProblemId(Long problemId);

    /**
     * 소프트 딜리트로 구현하였기 때문에, 대댓글을 포함한
     * 특정 문제의 삭제되지 않은 댓글 수를 조회합니다.
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.problem.problemId = :problemId AND c.isDeleted = false")
    Long countByProblemId(@Param("problemId") Long problemId);
}
