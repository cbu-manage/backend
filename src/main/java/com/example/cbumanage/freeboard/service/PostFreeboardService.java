package com.example.cbumanage.freeboard.service;

import com.example.cbumanage.freeboard.entity.PostFreeboard;
import com.example.cbumanage.freeboard.entity.enums.FreeboardTopic;
import com.example.cbumanage.freeboard.repository.PostFreeboardRepository;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.post.service.PostService;
import com.example.cbumanage.post.util.PostMapper;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;

    @Transactional
    public PostDTO.PostFreeboardCreateResponseDTO createFreeBoard(PostDTO.PostFreeboardCreateRequestDTO req, Long userId) {
        PostDTO.PostCreateDTO postCreateDTO = new PostDTO.PostCreateDTO(
                userId, req.title(), req.content(), PostCategory.FREEBOARD.getValue()
        );
        Post post = postService.createPost(postCreateDTO);
        PostFreeboard freeboard = PostFreeboard.create(post, req.isAnonymous(), req.topic());
        postFreeboardRepository.save(freeboard);
        return postMapper.toPostFreeboardCreateResponseDTO(post, freeboard);
    }

    public Page<PostDTO.PostFreeboardPreviewResponse> getFreeBoardList(Pageable pageable, Long userId, FreeboardTopic topic) {
        Page<PostFreeboard> page = topic == null
                ? postFreeboardRepository.findAllActive(pageable)
                : postFreeboardRepository.findAllActiveByTopic(topic, pageable);
        return page
                .map(fb -> fb.isAnonymous()
                        ? postMapper.toPostFreeboardAnonymousPreviewDTO(fb, userId)
                        : postMapper.toPostFreeboardPreviewDTO(fb));
    }

    @Transactional
    public PostDTO.PostFreeboardResponse getFreeBoard(Long postId, Long userId) {
        postFreeboardRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("FreeBoard Not Found"));
        postRepository.incrementViewCount(postId);
        // 벌크 UPDATE 는 영속성 컨텍스트를 건드리지 않는다. 비우지 않으면 아래 재조회가 캐시를 읽어
        // 방금 올린 조회수가 응답에 안 실린다(건의·소식과 같은 처리).
        entityManager.clear();
        PostFreeboard freeboard = postFreeboardRepository.findByPostId(postId)
                .orElseThrow(() -> new EntityNotFoundException("FreeBoard Not Found"));
        if (freeboard.isAnonymous()) {
            return postMapper.toPostFreeboardAnonymousInfoDTO(freeboard, userId);
        }
        return postMapper.toPostFreeboardInfoDTO(freeboard);
    }

    @Transactional
    public void updateFreeBoard(PostDTO.PostFreeboardUpdateDTO req, Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        if (!post.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "NOT POST OWNER");
        }
        postService.updatePost(new PostDTO.PostUpdateDTO(req.title(), req.content()), post);
        if (req.topic() != null) {
            PostFreeboard freeboard = postFreeboardRepository.findByPostId(postId)
                    .orElseThrow(() -> new EntityNotFoundException("FreeBoard Not Found"));
            freeboard.changeTopic(req.topic());
        }
    }

    /** 마이페이지 — 내가 쓴 글이므로 isAuthor 는 항상 true 가 된다 */
    public Page<PostDTO.PostFreeboardResponse> getMyFreeboards(Pageable pageable, Long userId) {
        return postFreeboardRepository.findByAuthorId(userId, pageable)
                .map(fb -> fb.isAnonymous()
                        ? postMapper.toPostFreeboardAnonymousInfoDTO(fb, userId)
                        : postMapper.toPostFreeboardInfoDTO(fb));
    }
}
