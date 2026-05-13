package com.example.cbumanage.news.service;

import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.news.dto.NewsDTO;
import com.example.cbumanage.news.entity.News;
import com.example.cbumanage.news.entity.enums.NewsCategory;
import com.example.cbumanage.news.repository.NewsRepository;
import com.example.cbumanage.post.entity.Post;
import com.example.cbumanage.post.entity.enums.PostCategory;
import com.example.cbumanage.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsService {

    private final NewsRepository newsRepository;
    private final PostRepository postRepository;

    public Page<NewsDTO.NewsListItemDTO> getNewsList(Pageable pageable, NewsCategory category) {
        boolean includeDefaultCategory = category == NewsCategory.NOTICE;
        List<News> pinned = newsRepository.findPinnedNews(category, includeDefaultCategory);
        Page<News> regular = newsRepository.findRegularNews(category, includeDefaultCategory, pageable);

        List<NewsDTO.NewsListItemDTO> items = new ArrayList<>(pinned.size() + regular.getNumberOfElements());
        pinned.forEach(news -> items.add(NewsDTO.NewsListItemDTO.from(news)));
        regular.forEach(news -> items.add(NewsDTO.NewsListItemDTO.from(news)));

        return new PageImpl<>(items, pageable, regular.getTotalElements());
    }

    @Transactional
    public NewsDTO.NewsDetailDTO getNewsDetail(Long newsId) {
        News news = findNewsOrThrow(newsId);
        postRepository.incrementViewCount(news.getPostId());
        Post post = postRepository.findById(news.getPostId())
                .orElseThrow(() -> new BaseException(ErrorCode.POST_NOT_FOUND));
        return NewsDTO.NewsDetailDTO.from(news, post.getViewCount());
    }

    @Transactional
    public NewsDTO.NewsDetailDTO createNews(NewsDTO.NewsCreateRequestDTO request, Long userId) {
        Post post = Post.create(userId, request.title(), request.content(), PostCategory.NEWS.getValue());
        postRepository.save(post);

        News news = News.create(post, request.category());
        newsRepository.save(news);

        return NewsDTO.NewsDetailDTO.from(news);
    }

    @Transactional
    public NewsDTO.NewsDetailDTO updateNews(Long newsId, NewsDTO.NewsUpdateRequestDTO request) {
        News news = findNewsOrThrow(newsId);
        news.change(request.title(), request.content(), request.category());
        newsRepository.flush();
        return NewsDTO.NewsDetailDTO.from(news);
    }

    @Transactional
    public void deleteNews(Long newsId) {
        News news = findNewsOrThrow(newsId);
        news.softDelete();
        newsRepository.delete(news);
    }

    @Transactional
    public NewsDTO.NewsDetailDTO changePinned(Long newsId, boolean pinned) {
        News news = findNewsOrThrow(newsId);
        news.changePinned(pinned);
        return NewsDTO.NewsDetailDTO.from(news);
    }

    public Page<NewsDTO.NewsListItemDTO> getMyNews(Pageable pageable, Long userId) {
        return newsRepository.findByAuthorId(userId, pageable)
                .map(NewsDTO.NewsListItemDTO::from);
    }

    private News findNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new BaseException(ErrorCode.NEWS_NOT_FOUND));
    }
}
