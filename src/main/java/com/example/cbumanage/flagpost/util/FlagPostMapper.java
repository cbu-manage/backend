package com.example.cbumanage.flagpost.util;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.entity.FlagPost;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlagPostMapper {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public FlagPostDTO.FlagPostCreateResponse toFlagPostCreateResponse(FlagPost flagPost) {
        return new FlagPostDTO.FlagPostCreateResponse(
                flagPost.getId(),
                flagPost.getPostId(),
                flagPost.getAuthorId(),
                flagPost.getContent(),
                flagPost.getCreatedAt()
        );
    }

    public FlagPostDTO.FlagPostInfoDTO toFlagPostInfoDTO(FlagPost flagPost) {
        Post targetPost = postRepository.findById(flagPost.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        User targetUser = userRepository.findById(targetPost.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Target User Not Found"));
        User author = userRepository.findById(flagPost.getAuthorId())
                .orElseThrow(() -> new EntityNotFoundException("Author Not Found"));

        return new FlagPostDTO.FlagPostInfoDTO(
                flagPost.getId(),
                flagPost.getContent(),
                flagPost.getCreatedAt(),
                targetPost.getId(),
                targetPost.getTitle(),
                targetPost.getContent(),
                targetUser.getUserId(),
                targetUser.getName(),
                targetUser.getGeneration(),
                author.getUserId(),
                author.getName(),
                author.getGeneration()
        );
    }
}
