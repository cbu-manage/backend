package com.example.cbumanage.news.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.example.cbumanage.global.error.BaseException;
import com.example.cbumanage.global.error.ErrorCode;
import com.example.cbumanage.news.dto.NewsDTO;
import com.example.cbumanage.news.entity.News;
import com.example.cbumanage.news.entity.NewsAttachment;
import com.example.cbumanage.news.repository.NewsAttachmentRepository;
import com.example.cbumanage.news.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsAttachmentService {

    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final long DOWNLOAD_URL_TTL_MILLIS = 5L * 60 * 1000;
    private static final String KEY_PREFIX = "uploads/news/";
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    private static final String DEFAULT_FILE_NAME = "attachment";

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp", "gif", "heic", "heif",
            "pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx",
            "hwp", "hwpx", "txt", "csv", "zip"
    );

    private final AmazonS3 amazonS3;
    private final NewsRepository newsRepository;
    private final NewsAttachmentRepository newsAttachmentRepository;

    @Value("${aws_bucket}")
    private String awsBucket;

    @Transactional
    public NewsDTO.NewsAttachmentDTO addAttachment(Long newsId, MultipartFile file) {
        News news = findNewsOrThrow(newsId);
        String fileName = resolveFileName(file.getOriginalFilename());
        validate(file, fileName);

        String key = KEY_PREFIX + newsId + "/" + UUID.randomUUID() + extensionWithDot(fileName);
        String contentType = file.getContentType() == null ? DEFAULT_CONTENT_TYPE : file.getContentType();
        upload(file, key, contentType);

        NewsAttachment attachment = newsAttachmentRepository.save(
                NewsAttachment.create(news, key, fileName, contentType, file.getSize())
        );
        return NewsDTO.NewsAttachmentDTO.from(attachment);
    }

    @Transactional
    public void deleteAttachment(Long newsId, Long attachmentId) {
        NewsAttachment attachment = findAttachmentOrThrow(newsId, attachmentId);
        amazonS3.deleteObject(awsBucket, attachment.getS3Key());
        newsAttachmentRepository.delete(attachment);
    }

    public NewsDTO.AttachmentDownloadDTO getDownloadUrl(Long newsId, Long attachmentId) {
        NewsAttachment attachment = findAttachmentOrThrow(newsId, attachmentId);
        URL url = presignedDownloadUrl(attachment);
        return new NewsDTO.AttachmentDownloadDTO(url.toString(), attachment.getOriginalFileName());
    }

    private void upload(MultipartFile file, String key, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(contentType);
        metadata.setContentLength(file.getSize());

        try (InputStream in = file.getInputStream()) {
            amazonS3.putObject(awsBucket, key, in, metadata);
        } catch (IOException e) {
            throw new BaseException(ErrorCode.FILE_PROCESS_FAILED);
        }
    }

    private URL presignedDownloadUrl(NewsAttachment attachment) {
        Date expiration = new Date(System.currentTimeMillis() + DOWNLOAD_URL_TTL_MILLIS);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(awsBucket, attachment.getS3Key())
                .withMethod(HttpMethod.GET)
                .withExpiration(expiration);
        request.setResponseHeaders(new ResponseHeaderOverrides()
                .withContentDisposition(contentDisposition(attachment.getOriginalFileName()))
                .withContentType(attachment.getContentType()));
        return amazonS3.generatePresignedUrl(request);
    }

    private void validate(MultipartFile file, String fileName) {
        if (file.isEmpty()) {
            throw new BaseException(ErrorCode.INVALID_REQUEST);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException(ErrorCode.NEWS_ATTACHMENT_SIZE_EXCEEDED);
        }
        if (!ALLOWED_EXTENSIONS.contains(extension(fileName))) {
            throw new BaseException(ErrorCode.NEWS_ATTACHMENT_TYPE_NOT_ALLOWED);
        }
    }

    private News findNewsOrThrow(Long newsId) {
        return newsRepository.findById(newsId)
                .orElseThrow(() -> new BaseException(ErrorCode.NEWS_NOT_FOUND));
    }

    private NewsAttachment findAttachmentOrThrow(Long newsId, Long attachmentId) {
        findNewsOrThrow(newsId);
        NewsAttachment attachment = newsAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new BaseException(ErrorCode.NEWS_ATTACHMENT_NOT_FOUND));
        if (!attachment.belongsTo(newsId)) {
            throw new BaseException(ErrorCode.NEWS_ATTACHMENT_NOT_FOUND);
        }
        return attachment;
    }

    private String resolveFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return DEFAULT_FILE_NAME;
        }
        return originalFilename;
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase();
    }

    private String extensionWithDot(String fileName) {
        String extension = extension(fileName);
        return extension.isEmpty() ? "" : "." + extension;
    }

    private String contentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String asciiFallback = fileName.replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "");
        return "attachment; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded;
    }
}
