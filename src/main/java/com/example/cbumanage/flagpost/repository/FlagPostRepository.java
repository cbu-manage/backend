package com.example.cbumanage.flagpost.repository;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.entity.FlagPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlagPostRepository extends JpaRepository<FlagPost, Long> {

    Optional<FlagPost> findByIdAndIsDeletedFalse(Long id);

    List<FlagPost> findAllByPostIdAndIsDeletedFalse(Long postId);

    @Modifying
    @Query("UPDATE FlagPost f SET f.isDeleted = true WHERE f.postId = :postId AND f.isDeleted = false")
    void softDeleteAllByPostId(@Param("postId") Long postId);

    @Query(value = """
            select new com.example.cbumanage.flagpost.dto.FlagPostDTO$FlagPostPreviewDTO(
                f.id, f.content, f.createdAt,
                p.id, p.title,
                u.userId, u.name, u.generation
            )
            from FlagPost f
            join Post p on p.id = f.postId
            join User u on u.userId = f.authorId
            where f.isDeleted = false
            """,
            countQuery = """
            select count(f)
            from FlagPost f
            where f.isDeleted = false
            """)
    Page<FlagPostDTO.FlagPostPreviewDTO> findFlagPostPreviews(Pageable pageable);
}
