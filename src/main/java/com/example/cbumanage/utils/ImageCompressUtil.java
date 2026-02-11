package com.example.cbumanage.utils;

import net.coobird.thumbnailator.Thumbnails;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCompressUtil {

    public static byte[] compressToJpeg(InputStream input,
                                        int maxW,
                                        int maxH,
                                        float quality) throws IOException {

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(input)
                    .size(maxW, maxH)
                    .useExifOrientation(true)
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toOutputStream(out);

            return out.toByteArray();
        }
    }
}
