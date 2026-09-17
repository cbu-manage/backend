package com.example.cbumanage.post.service;

import com.example.cbumanage.freeboard.repository.PostFreeboardRepository;
import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.post.util.PostMapper;
import com.example.cbumanage.user.entity.Role;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
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
public class PostService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserRepository userRepository;
    private final PostFreeboardRepository postFreeboardRepository;

    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        User author = userRepository.findById(postCreateDTO.authorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getUserId(), postCreateDTO.title(), postCreateDTO.content(), postCreateDTO.category());
        Post saved = postRepository.save(post);
        return saved;
    }


    public Page<PostDTO.PostInfoDTO> getPostsByCategory(Pageable pageable,int category){
        Page<Post> posts=postRepository.findByCategoryAndIsDeletedFalse(category,pageable);
        return posts.map(this::toPostInfoDTO);
    }


    public PostDTO.PostInfoDTO getPostById(Long postId){
        // 목록은 isDeleted 를 걸렀지만 상세는 안 걸러서, 지운 글을 id 로 직접 부르면 본문이 그대로 나왔다.
        Post post=postRepository.findByIdAndIsDeletedFalse(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        return toPostInfoDTO(post);
    }

    /** 공통 DTO. 익명이 강제된 글이면 작성자 필드를 비워서 어느 경로로도 이름이 새지 않게 한다 */
    private PostDTO.PostInfoDTO toPostInfoDTO(Post post) {
        return isAuthorHidden(post)
                ? postMapper.toAnonymousPostInfoDTO(post)
                : postMapper.toPostInfoDTO(post);
    }

    /** 작성자를 숨겨야 하는 글 — 건의 글 전부, 익명 자유게시판 글 */
    private boolean isAuthorHidden(Post post) {
        if (post.getCategory() == PostCategory.SUGGESTION.getValue()) {
            return true;
        }
        return postFreeboardRepository.findByPostId(post.getId())
                .map(fb -> fb.isAnonymous())
                .orElse(false);
    }

    /*
    updatePostReport 하나의 메소드에 들어온 req 을 각각의 엔티티에 맞춰
    두개의 DTO 로 분리해 각 엔티티의 update 를 수행합니다
     Setter 를 사용하지 않고 클래스 내부에 변환메소드를 만들어 사용합니다
     */
    public void updatePost(PostDTO.PostUpdateDTO postUpdateDTO,Post post) {
        if (postUpdateDTO.title() != null) {
            post.changeTitle(postUpdateDTO.title());
        }
        if (postUpdateDTO.content() != null) {
            post.changeContent(postUpdateDTO.content());
        }
    }

    @Transactional
    public void softDeletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        boolean isAdminOrManager = user.getRole().isPresidentOrVicePresidentOrAdmin() || user.getRole() == Role.ROLE_MANAGER;
        if (!isAdminOrManager && !post.getAuthorId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        post.delete();
    }

    public Page<PostDTO.PostMyPageViewDTO>  getMyPosts(Pageable pageable,Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Page<Post> posts = postRepository.findByAuthorIdAndIsDeletedFalse(userId,pageable);
        return posts.map(post -> postMapper.toPostMyPageViewDTO(post, user));
    }
}
