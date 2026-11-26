package com.vibesnapshot.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ImageCompareServiceTest {

    private final ImageCompareService service = new ImageCompareService();

    @Test
    void identicalImagesScore100Percent(@TempDir Path tempDir) throws Exception {
        Path img1 = createSolidPng(tempDir.resolve("golden.png"), 200, 100, Color.BLUE);
        Path img2 = createSolidPng(tempDir.resolve("sub.png"), 200, 100, Color.BLUE);
        Path diff = tempDir.resolve("diff.png");

        ImageCompareService.ComparisonResult result = service.compareImages(img1, img2, diff);

        assertEquals(100.0, result.similarity());
        assertEquals(0L, result.diffPixels());
        assertEquals(20_000L, result.totalPixels());
        assertEquals(20_000L, result.matchedPixels());
        assertTrue(Files.exists(diff));
    }

    @Test
    void contrastingImagesScore0Percent(@TempDir Path tempDir) throws Exception {
        Path img1 = createSolidPng(tempDir.resolve("golden.png"), 100, 100, Color.WHITE);
        Path img2 = createSolidPng(tempDir.resolve("sub.png"), 100, 100, Color.BLACK);
        Path diff = tempDir.resolve("diff.png");

        ImageCompareService.ComparisonResult result = service.compareImages(img1, img2, diff);

        assertEquals(0.0, result.similarity());
        assertEquals(10_000L, result.diffPixels());
        assertEquals(0L, result.matchedPixels());
        assertTrue(Files.exists(diff));
    }

    @Test
    void oversizedGoldenImageCapsAt1280x800(@TempDir Path tempDir) throws Exception {
        Path img1 = createSolidPng(tempDir.resolve("golden.png"), 1600, 1000, Color.DARK_GRAY);
        Path img2 = createSolidPng(tempDir.resolve("sub.png"), 800, 600, Color.DARK_GRAY);
        Path diff = tempDir.resolve("diff.png");

        ImageCompareService.ComparisonResult result = service.compareImages(img1, img2, diff);

        assertEquals(1280, result.width());
        assertEquals(800, result.height());
        assertEquals(1280L * 800L, result.totalPixels());
        assertEquals(100.0, result.similarity());
    }

    private Path createSolidPng(Path path, int width, int height, Color color) throws Exception {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        ImageIO.write(img, "png", path.toFile());
        return path;
    }
}
