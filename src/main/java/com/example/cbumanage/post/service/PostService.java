package com.example.cbumanage.post.service;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.post.entity.Post;
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

    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        User author = userRepository.findById(postCreateDTO.authorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getUserId(), postCreateDTO.title(), postCreateDTO.content(), postCreateDTO.category());
        Post saved = postRepository.save(post);
        return saved;
    }


    public Page<PostDTO.PostInfoDTO> getPostsByCategory(Pageable pageable,int category){
        Page<Post> posts=postRepository.findByCategoryAndIsDeletedFalse(category,pageable);
        return posts.map(post->postMapper.toPostInfoDTO(post));
    }


    public PostDTO.PostInfoDTO getPostById(Long postId){
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        return postMapper.toPostInfoDTO(post);
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
