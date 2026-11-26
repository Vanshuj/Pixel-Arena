package com.vibesnapshot.service;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class ImageCompareService {

    private static final int MAX_WIDTH = 1280;
    private static final int MAX_HEIGHT = 800;
    private static final double THRESHOLD = 0.1;
    private static final double ALPHA = 0.3;
    private static final int[] DIFF_COLOR = {255, 50, 50};
    private static final int[] AA_COLOR = {255, 200, 0};

    public ComparisonResult compareImages(Path goldenPath, Path submissionPath, Path diffOutputPath) throws IOException {
        BufferedImage goldenSrc = ImageIO.read(goldenPath.toFile());
        BufferedImage submissionSrc = ImageIO.read(submissionPath.toFile());

        if (goldenSrc == null) {
            throw new IOException("Unable to decode golden image: " + goldenPath);
        }
        if (submissionSrc == null) {
            throw new IOException("Unable to decode submission image: " + submissionPath);
        }

        int targetWidth = Math.min(goldenSrc.getWidth(), MAX_WIDTH);
        int targetHeight = Math.min(goldenSrc.getHeight(), MAX_HEIGHT);

        BufferedImage goldenResized = resizeImage(goldenSrc, targetWidth, targetHeight);
        BufferedImage submissionResized = resizeImage(submissionSrc, targetWidth, targetHeight);

        int[] img1 = goldenResized.getRGB(0, 0, targetWidth, targetHeight, null, 0, targetWidth);
        int[] img2 = submissionResized.getRGB(0, 0, targetWidth, targetHeight, null, 0, targetWidth);
        int[] diffPixelsOut = new int[targetWidth * targetHeight];

        long numDiffPixels = pixelmatch(img1, img2, diffPixelsOut, targetWidth, targetHeight);

        BufferedImage diffImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        diffImage.setRGB(0, 0, targetWidth, targetHeight, diffPixelsOut, 0, targetWidth);

        File outFile = diffOutputPath.toFile();
        if (outFile.getParentFile() != null) {
            outFile.getParentFile().mkdirs();
        }
        ImageIO.write(diffImage, "png", outFile);

        long totalPixels = (long) targetWidth * targetHeight;
        long matchedPixels = totalPixels - numDiffPixels;
        double similarity = Math.round(((double) matchedPixels / totalPixels) * 100.0 * 100.0) / 100.0;

        return new ComparisonResult(
                similarity,
                numDiffPixels,
                totalPixels,
                matchedPixels,
                targetWidth,
                targetHeight
        );
    }

    private BufferedImage resizeImage(BufferedImage src, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, width, height, null);
        g.dispose();
        return resized;
    }

    /**
     * Faithful Java port of pixelmatch v5.3.0 (threshold=0.1, includeAA=false, alpha=0.3).
     */
    private long pixelmatch(int[] img1, int[] img2, int[] output, int width, int height) {
        double maxDelta = 35215.0 * THRESHOLD * THRESHOLD;
        long diffCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pos = y * width + x;
                double delta = colorDelta(img1, img2, pos, pos, false);

                if (Math.abs(delta) > maxDelta) {
                    if (antialiased(img1, x, y, width, height, img2)
                            || antialiased(img2, x, y, width, height, img1)) {
                        output[pos] = packRgba(AA_COLOR[0], AA_COLOR[1], AA_COLOR[2], 255);
                    } else {
                        output[pos] = packRgba(DIFF_COLOR[0], DIFF_COLOR[1], DIFF_COLOR[2], 255);
                        diffCount++;
                    }
                } else {
                    output[pos] = drawGrayPixel(img1[pos], ALPHA);
                }
            }
        }
        return diffCount;
    }

    private boolean antialiased(int[] img, int x1, int y1, int width, int height, int[] img2) {
        int x0 = Math.max(x1 - 1, 0);
        int y0 = Math.max(y1 - 1, 0);
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);
        int pos = y1 * width + x1;
        int zeroes = (x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2) ? 1 : 0;
        double min = 0;
        double max = 0;
        int minX = 0;
        int minY = 0;
        int maxX = 0;
        int maxY = 0;

        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                if (x == x1 && y == y1) {
                    continue;
                }
                double delta = colorDelta(img, img, pos, y * width + x, true);
                if (delta == 0) {
                    zeroes++;
                    if (zeroes > 2) {
                        return false;
                    }
                } else if (delta < min) {
                    min = delta;
                    minX = x;
                    minY = y;
                } else if (delta > max) {
                    max = delta;
                    maxX = x;
                    maxY = y;
                }
            }
        }

        if (min == 0 || max == 0) {
            return false;
        }

        return (hasManySiblings(img, minX, minY, width, height)
                && hasManySiblings(img2, minX, minY, width, height))
                || (hasManySiblings(img, maxX, maxY, width, height)
                && hasManySiblings(img2, maxX, maxY, width, height));
    }

    private boolean hasManySiblings(int[] img, int x1, int y1, int width, int height) {
        int x0 = Math.max(x1 - 1, 0);
        int y0 = Math.max(y1 - 1, 0);
        int x2 = Math.min(x1 + 1, width - 1);
        int y2 = Math.min(y1 + 1, height - 1);
        int pos = y1 * width + x1;
        int zeroes = (x1 == x0 || x1 == x2 || y1 == y0 || y1 == y2) ? 1 : 0;

        for (int x = x0; x <= x2; x++) {
            for (int y = y0; y <= y2; y++) {
                if (x == x1 && y == y1) {
                    continue;
                }
                int pos2 = y * width + x;
                if (img[pos] == img[pos2]) {
                    zeroes++;
                }
                if (zeroes > 2) {
                    return true;
                }
            }
        }
        return false;
    }

    private double colorDelta(int[] img1, int[] img2, int k, int m, boolean yOnly) {
        int p1 = img1[k];
        int p2 = img2[m];
        if (p1 == p2) {
            return 0;
        }

        double r1 = (p1 >> 16) & 0xFF;
        double g1 = (p1 >> 8) & 0xFF;
        double b1 = p1 & 0xFF;
        double a1 = (p1 >>> 24) & 0xFF;

        double r2 = (p2 >> 16) & 0xFF;
        double g2 = (p2 >> 8) & 0xFF;
        double b2 = p2 & 0xFF;
        double a2 = (p2 >>> 24) & 0xFF;

        if (a1 < 255) {
            double alpha1 = a1 / 255.0;
            r1 = blend(r1, alpha1);
            g1 = blend(g1, alpha1);
            b1 = blend(b1, alpha1);
        }
        if (a2 < 255) {
            double alpha2 = a2 / 255.0;
            r2 = blend(r2, alpha2);
            g2 = blend(g2, alpha2);
            b2 = blend(b2, alpha2);
        }

        double dr = r1 - r2;
        double dg = g1 - g2;
        double db = b1 - b2;

        double y = dr * 0.29889531 + dg * 0.58662247 + db * 0.11448223;
        if (yOnly) {
            return y;
        }

        double i = dr * 0.59597799 - dg * 0.27417610 - db * 0.32180189;
        double q = dr * 0.21147017 - dg * 0.52261711 + db * 0.31114694;

        double delta = 0.5053 * y * y + 0.299 * i * i + 0.1957 * q * q;
        return y > 0 ? -delta : delta;
    }

    private double blend(double c, double a) {
        return 255.0 + (c - 255.0) * a;
    }

    private int drawGrayPixel(int argb, double alpha) {
        int a = (argb >>> 24) & 0xFF;
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        double luma = r * 0.29889531 + g * 0.58662247 + b * 0.11448223;
        int val = (int) Math.round(blend(luma, alpha * a / 255.0));
        val = Math.max(0, Math.min(255, val));
        return packRgba(val, val, val, 255);
    }

    private int packRgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public record ComparisonResult(
            double similarity,
            long diffPixels,
            long totalPixels,
            long matchedPixels,
            int width,
            int height
    ) {
    }
}
