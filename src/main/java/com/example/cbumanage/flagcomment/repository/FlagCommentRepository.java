package com.example.cbumanage.flagcomment.repository;

import com.example.cbumanage.flagcomment.entity.FlagComment;
import com.example.cbumanage.flagcomment.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlagCommentRepository extends JpaRepository<FlagComment, Long> {

    Optional<FlagComment> findByIdAndIsDeletedFalse(Long id);

    boolean existsByAuthorIdAndCommentIdAndIsDeletedFalse(Long authorId, Long commentId);

    @Modifying
    @Query("UPDATE FlagComment f SET f.isDeleted = true WHERE f.commentId = :commentId AND f.isDeleted = false")
    void softDeleteAllByCommentId(@Param("commentId") Long commentId);

    @Query(value = """
            select new com.example.cbumanage.flagcomment.dto.CommentDTO$FlagCommentPreviewDTO(
                f.id, f.content, f.createdAt,
                c.id, c.content,
                u.userId, u.name, u.generation
            )
            from FlagComment f
            join Comment c on c.id = f.commentId
            join User u on u.userId = f.authorId
            where f.isDeleted = false
            """,
            countQuery = """
            select count(f)
            from FlagComment f
            where f.isDeleted = false
            """)
    Page<CommentDTO.FlagCommentPreviewDTO> findFlagCommentPreviews(Pageable pageable);
}
