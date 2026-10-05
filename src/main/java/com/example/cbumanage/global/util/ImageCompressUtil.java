package com.example.cbumanage.global.util;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.filters.Canvas;
import net.coobird.thumbnailator.geometry.Positions;

import java.awt.Color;
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

    /**
     * 사진을 canvasW×canvasH 흰 캔버스 안에 비율을 유지한 채 가운데 맞춰 넣는다.
     * 결과 크기는 항상 canvasW×canvasH 라서, 고정 크기 그림 틀에 넣어도 사진이 늘어나거나 찌그러지지 않는다.
     */
    public static byte[] fitToCanvasJpeg(InputStream input,
                                         int canvasW,
                                         int canvasH,
                                         float quality) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Thumbnails.of(input)
                    .size(canvasW, canvasH)               // 비율 유지, 캔버스 안에 들어가게 축소
                    .useExifOrientation(true)
                    .addFilter(new Canvas(canvasW, canvasH, Positions.CENTER, Color.WHITE))
                    .outputFormat("jpg")
                    .outputQuality(quality)
                    .toOutputStream(out);
            return out.toByteArray();
        }
    }
}
