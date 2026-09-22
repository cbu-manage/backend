package com.example.cbumanage.flagpost.util;

import com.example.cbumanage.flagpost.dto.FlagPostDTO;
import com.example.cbumanage.flagpost.entity.FlagPost;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import com.example.cbumanage.post.util.AnonymityResolver;
import com.example.cbumanage.user.entity.User;
import com.example.cbumanage.user.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FlagPostMapperTest {

    private final PostRepository postRepository = mock(PostRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final AnonymityResolver anonymityResolver = mock(AnonymityResolver.class);
    private final FlagPostMapper mapper = new FlagPostMapper(postRepository, userRepository, anonymityResolver);

    private FlagPost flagPost() {
        FlagPost flagPost = mock(FlagPost.class);
        when(flagPost.getId()).thenReturn(10L);
        when(flagPost.getPostId()).thenReturn(20L);
        when(flagPost.getAuthorId()).thenReturn(30L);
        when(flagPost.getContent()).thenReturn("비방입니다");
        return flagPost;
    }

    private Post targetPost(int category) {
        Post post = mock(Post.class);
        when(post.getId()).thenReturn(20L);
        when(post.getAuthorId()).thenReturn(40L);
        when(post.getTitle()).thenReturn("제목");
        when(post.getContent()).thenReturn("본문");
        when(post.getCategory()).thenReturn(category);
        return post;
    }

    private User user(Long id, String name) {
        User user = mock(User.class);
        when(user.getUserId()).thenReturn(id);
        when(user.getName()).thenReturn(name);
        when(user.getGeneration()).thenReturn(7L);
        return user;
    }

    @Test
    void 익명글_신고상세는_작성자를_가리고_본문은_그대로_준다() {
        Post post = targetPost(PostCategory.SUGGESTION.getValue());
        User reporter = user(30L, "신고자");
        when(postRepository.findById(20L)).thenReturn(Optional.of(post));
        when(userRepository.findById(30L)).thenReturn(Optional.of(reporter));
        when(anonymityResolver.isAnonymous(any(Post.class))).thenReturn(true);

        FlagPostDTO.FlagPostInfoDTO dto = mapper.toFlagPostInfoDTO(flagPost());

        assertThat(dto.targetUserId()).isNull();
        assertThat(dto.targetUserName()).isNull();
        assertThat(dto.targetUserGeneration()).isNull();
        assertThat(dto.targetPostContent()).isEqualTo("본문");
        assertThat(dto.authorName()).isEqualTo("신고자");
    }

    @Test
    void 공개글_신고상세는_작성자를_그대로_준다() {
        Post post = targetPost(PostCategory.NEWS.getValue());
        User reporter = user(30L, "신고자");
        User writer = user(40L, "작성자");
        when(postRepository.findById(20L)).thenReturn(Optional.of(post));
        when(userRepository.findById(30L)).thenReturn(Optional.of(reporter));
        when(userRepository.findById(40L)).thenReturn(Optional.of(writer));
        when(anonymityResolver.isAnonymous(any(Post.class))).thenReturn(false);

        FlagPostDTO.FlagPostInfoDTO dto = mapper.toFlagPostInfoDTO(flagPost());

        assertThat(dto.targetUserId()).isEqualTo(40L);
        assertThat(dto.targetUserName()).isEqualTo("작성자");
    }
}
