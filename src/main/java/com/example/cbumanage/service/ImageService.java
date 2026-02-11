package com.example.cbumanage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.cbumanage.utils.ImageCompressUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.UUID;


@Service
public class ImageService {

    private static final Set<String> ALLOWED = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/heic",
            "image/heif"
    );

    private final AmazonS3 amazonS3;

    @Autowired
    public ImageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    @Value("${aws_bucket}")
    String awsBucket;

    public String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String type = file.getContentType();
        if (type == null || !ALLOWED.contains(type)) {
            throw new IllegalArgumentException("Invalid file type");
        }

        byte[] compressed;
        try (InputStream in = file.getInputStream()) {
            compressed = ImageCompressUtil.compressToJpeg(in, 2048, 2048, 0.82f);
        }

        String key = "uploads/" + UUID.randomUUID() + ".jpg";

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("image/jpeg");
        metadata.setContentLength(compressed.length);

        try (InputStream upload = new ByteArrayInputStream(compressed)) {
            amazonS3.putObject(awsBucket, key, upload, metadata);
        }

        return amazonS3.getUrl(awsBucket, key).toString();
    }
}