package com.example.cbumanage.global.util;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ImageCompressUtilTest {

    @Test
    void 가로_사진은_세로형_캔버스에_비율을_유지한_채_가운데_들어간다() throws Exception {
        byte[] out = ImageCompressUtil.fitToCanvasJpeg(png(1049, 631, Color.BLUE), 1083, 1200, 0.85f);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(out));

        assertEquals(1083, img.getWidth());
        assertEquals(1200, img.getHeight());
        // 위아래는 흰 여백, 가운데는 사진
        assertTrue(isWhite(img.getRGB(540, 10)));
        assertTrue(isWhite(img.getRGB(540, 1190)));
        assertTrue(isBlue(img.getRGB(540, 600)));
        // 가로는 캔버스에 꽉 참 (1083 폭에 맞춰 축소)
        assertTrue(isBlue(img.getRGB(5, 600)));
        assertTrue(isBlue(img.getRGB(1078, 600)));
    }

    @Test
    void 세로_사진은_좌우에_여백이_생긴다() throws Exception {
        byte[] out = ImageCompressUtil.fitToCanvasJpeg(png(600, 1200, Color.BLUE), 1083, 1200, 0.85f);
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(out));

        assertEquals(1083, img.getWidth());
        assertEquals(1200, img.getHeight());
        assertTrue(isWhite(img.getRGB(10, 600)));
        assertTrue(isWhite(img.getRGB(1073, 600)));
        assertTrue(isBlue(img.getRGB(540, 600)));
    }

    private static ByteArrayInputStream png(int w, int h, Color fill) throws Exception {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(fill);
        g.fillRect(0, 0, w, h);
        g.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return new ByteArrayInputStream(out.toByteArray());
    }

    private static boolean isWhite(int rgb) {
        Color c = new Color(rgb);
        return c.getRed() > 240 && c.getGreen() > 240 && c.getBlue() > 240;
    }

    private static boolean isBlue(int rgb) {
        Color c = new Color(rgb);
        return c.getBlue() > 200 && c.getRed() < 60 && c.getGreen() < 60;
    }
}
