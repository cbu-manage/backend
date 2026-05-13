package com.example.cbumanage.freeboard.service;

import com.example.cbumanage.freeboard.entity.PostFreeboard;
import com.example.cbumanage.freeboard.repository.PostFreeboardRepository;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.post.util.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostFreeboardService {

    private final PostFreeboardRepository postFreeboardRepository;
    private final PostRepository postRepository;
    private final PostService postService;
    private final PostMapper postMapper;

    @Transactional
    public PostDTO.PostFreeboardCreateResponseDTO createFreeBoard(PostDTO.PostFreeboardCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = new PostDTO.PostCreateDTO(
                userId, req.title(), req.content(), PostCategory.FREEBOARD.getValue()
        );
        Post post = postService.createPost(postCreateDTO);
        PostFreeboard freeboard = PostFreeboard.create(post, req.isAnonymous());
        postFreeboardRepository.save(freeboard);
        return postMapper.toPostFreeboardCreateResponseDTO(post, freeboard);
    }

    public Page<Object> getFreeBoardList(Pageable pageable) {
        return postFreeboardRepository.findAllActive(pageable)
                .map(fb -> fb.isAnonymous()
                        ? postMapper.toPostFreeboardAnonymousInfoDTO(fb)
                        : postMapper.toPostFreeboardInfoDTO(fb));
    }

    public Object getFreeBoard(Long postId) {
        PostFreeboard freeboard = postFreeboardRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("FreeBoard Not Found"));
        if (freeboard.isAnonymous()) {
            return postMapper.toPostFreeboardAnonymousInfoDTO(freeboard);
        }
        return postMapper.toPostFreeboardInfoDTO(freeboard);
    }

    @Transactional
    public void updateFreeBoard(PostDTO.PostUpdateDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        if (!post.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT POST OWNER");
        }
        postService.updatePost(req, post);
    }

    public Page<Object> getMyFreeboards(Pageable pageable, Long userId) {
        return postFreeboardRepository.findByAuthorId(userId, pageable)
                .map(fb -> fb.isAnonymous()
                        ? postMapper.toPostFreeboardAnonymousInfoDTO(fb)
                        : postMapper.toPostFreeboardInfoDTO(fb));
    }
}
