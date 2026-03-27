package com.example.cbumanage.post.service;

import com.example.cbumanage.post.dto.PostDTO;
import com.example.cbumanage.member.entity.CbuMember;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.member.repository.CbuMemberRepository;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.report.repository.PostReportRepository;
import com.example.cbumanage.group.repository.GroupRepository;
import com.example.cbumanage.group.repository.GroupMemberRepository;
import com.example.cbumanage.post.util.PostMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class PostService {

    private PostRepository postRepository;
    private PostReportRepository postReportRepository;
    private PostMapper postMapper;
    private CbuMemberRepository cbuMemberRepository;
    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;

    public Post createPost(PostDTO.PostCreateDTO postCreateDTO) {
        CbuMember author = cbuMemberRepository.findById(postCreateDTO.getAuthorId()).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Post post = Post.create(author.getCbuMemberId(), postCreateDTO.getTitle(), postCreateDTO.getContent(), postCreateDTO.getCategory());
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
        if (postUpdateDTO.getTitle() != null) {
            post.changeTitle(postUpdateDTO.getTitle());
        }
        if (postUpdateDTO.getContent() != null) {
            post.changeContent(postUpdateDTO.getContent());
        }
    }

    @Transactional
    public void softDeletePost(Long postId) {
        Post post=postRepository.findById(postId).orElseThrow(() -> new EntityNotFoundException("Post Not Found"));
        post.delete();
    }

    public Page<PostDTO.PostMyPageViewDTO>  getMyPosts(Pageable pageable,Long userId) {
        CbuMember cbuMember = cbuMemberRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User Not Found"));
        Page<Post> posts = postRepository.findByAuthorIdAndIsDeletedFalse(userId,pageable);
        return posts.map(post -> postMapper.toPostMyPageViewDTO(post,cbuMember));
    }



}
