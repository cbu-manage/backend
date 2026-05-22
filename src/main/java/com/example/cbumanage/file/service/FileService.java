package com.example.cbumanage.file.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cbumanage.global.util.ImageCompressUtil;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.Loader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/heic",
            "image/heif"
    );

    private static final long MAX_PDF_SIZE = 10L * 1024 * 1024; // 10MB

    private final AmazonS3 amazonS3;

    @Value("${aws_bucket}")
    private String awsBucket;

    public FileService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("FILE_EMPTY");
        }

        String type = file.getContentType();
        if (type == null || !ALLOWED_IMAGE_TYPES.contains(type)) {
            throw new IllegalArgumentException("INVALID_FILE_TYPE");
        }

        byte[] compressed;
        try (InputStream in = file.getInputStream()) {
            compressed = ImageCompressUtil.compressToJpeg(in, 2048, 2048, 0.82f);
        }

        String key = "uploads/image/" + UUID.randomUUID() + ".jpg";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(compressed.length);

        try (InputStream upload = new ByteArrayInputStream(compressed)) {
            amazonS3.putObject(awsBucket, key, upload, metadata);
        }

        return amazonS3.getUrl(awsBucket, key).toString();
    }

    public String uploadPdf(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("FILE_EMPTY");
        }

        String type = file.getContentType();
        if (!"application/pdf".equals(type)) {
            throw new IllegalArgumentException("INVALID_FILE_TYPE");
        }

        if (file.getSize() > MAX_PDF_SIZE) {
            throw new IllegalArgumentException("FILE_SIZE_EXCEEDED");
        }

        byte[] compressed = compressPdf(file.getBytes());

        String key = "uploads/pdf/" + UUID.randomUUID() + ".pdf";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");
        metadata.setContentLength(compressed.length);

        try (InputStream upload = new ByteArrayInputStream(compressed)) {
            amazonS3.putObject(awsBucket, key, upload, metadata);
        }

        return amazonS3.getUrl(awsBucket, key).toString();
    }

    private byte[] compressPdf(byte[] input) throws IOException {
        try (PDDocument doc = Loader.loadPDF(input);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            doc.save(out);
            return out.toByteArray();
        }
    }
}
