package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Pages;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.ComicDownloadService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComicDownloadServiceImpl implements ComicDownloadService {

    private final ComicRepository comicRepository;
    private final ChapterRepository chapterRepository;
    private final PageRepository pageRepository;
    private final ImageUrlResolver imageUrlResolver;

    @Override
    public byte[] downloadComicWithWatermark(Integer comicId, Integer userId) throws IOException {
        log.info("Starting download for comic {} by user {}", comicId, userId);
        
        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new RuntimeException("Comic not found: " + comicId));

        List<Chapters> chapters = chapterRepository.findByComic_ComicIdOrderByChapterNumberDesc(comicId);
        log.info("Found {} chapters for comic {}", chapters.size(), comicId);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            int totalPages = 0;
            for (Chapters chapter : chapters) {
                List<Pages> pages = pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapter.getChapterId());
                log.info("Processing chapter {} with {} pages", chapter.getChapterNumber(), pages.size());

                for (Pages page : pages) {
                    try {
                        String imageUrl = imageUrlResolver.resolve(page.getImgPath());
                        log.debug("Downloading image from: {}", imageUrl);
                        
                        byte[] watermarkedImage = downloadAndWatermark(imageUrl, userId);

                        String zipPath = String.format("%s/Chapter_%03d/page_%03d.jpg",
                                sanitizeFileName(comic.getTitle()),
                                chapter.getChapterNumber(),
                                page.getPageNumber());

                        ZipEntry entry = new ZipEntry(zipPath);
                        zos.putNextEntry(entry);
                        zos.write(watermarkedImage);
                        zos.closeEntry();
                        totalPages++;
                    } catch (Exception e) {
                        log.error("Failed to process page {} of chapter {}: {}", 
                                page.getPageNumber(), chapter.getChapterNumber(), e.getMessage());
                        throw e;
                    }
                }
            }
            log.info("Successfully created ZIP with {} pages", totalPages);
        }

        return baos.toByteArray();
    }

    private byte[] downloadAndWatermark(String imageUrl, Integer userId) throws IOException {
        log.info("Downloading and watermarking image: {}", imageUrl);
        
        try {
            // Download image from Cloudinary
            URL url = new URL(imageUrl);
            BufferedImage originalImage = null;
            
            // Try to download with timeout settings
            try {
                java.net.URLConnection connection = url.openConnection();
                connection.setConnectTimeout(30000); // 30 seconds
                connection.setReadTimeout(30000);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                
                try (InputStream is = connection.getInputStream()) {
                    originalImage = ImageIO.read(is);
                }
            } catch (IOException e) {
                log.error("Failed to open stream from URL: {}", imageUrl, e);
                throw new IOException("Cannot download image from: " + imageUrl + " - Error: " + e.getMessage(), e);
            }

            if (originalImage == null) {
                log.error("ImageIO.read returned null for URL: {}", imageUrl);
                throw new IOException("Failed to read image from URL (ImageIO returned null, possibly unsupported format): " + imageUrl);
            }

            log.info("Image downloaded successfully, size: {}x{}", originalImage.getWidth(), originalImage.getHeight());

            // Apply invisible watermark (LSB steganography)
            BufferedImage watermarked = applyInvisibleWatermark(originalImage, userId);

            // Convert to JPEG bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean written = ImageIO.write(watermarked, "jpg", baos);
            
            if (!written) {
                throw new IOException("Failed to write watermarked image as JPEG");
            }
            
            log.info("Watermarked image created, size: {} bytes", baos.size());
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error processing image: {}", imageUrl, e);
            throw new IOException("Failed to process image: " + imageUrl + " - Error: " + e.getMessage(), e);
        }
    }

    /**
     * Apply invisible watermark using LSB (Least Significant Bit) steganography.
     * Embeds userId into the least significant bits of pixel RGB values.
     */
    private BufferedImage applyInvisibleWatermark(BufferedImage image, Integer userId) {
        BufferedImage watermarked = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = watermarked.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        // Convert userId to binary string (32 bits)
        String watermarkData = String.format("%32s", Integer.toBinaryString(userId)).replace(' ', '0');

        // Embed watermark in first 32 pixels (1 bit per RGB channel = 3 bits per pixel)
        int bitIndex = 0;
        outerLoop:
        for (int y = 0; y < watermarked.getHeight() && bitIndex < watermarkData.length(); y++) {
            for (int x = 0; x < watermarked.getWidth() && bitIndex < watermarkData.length(); x++) {
                int rgb = watermarked.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Embed 1 bit in each channel's LSB
                if (bitIndex < watermarkData.length()) {
                    r = (r & 0xFE) | (watermarkData.charAt(bitIndex++) - '0');
                }
                if (bitIndex < watermarkData.length()) {
                    g = (g & 0xFE) | (watermarkData.charAt(bitIndex++) - '0');
                }
                if (bitIndex < watermarkData.length()) {
                    b = (b & 0xFE) | (watermarkData.charAt(bitIndex++) - '0');
                }

                int newRgb = (r << 16) | (g << 8) | b;
                watermarked.setRGB(x, y, newRgb);

                if (bitIndex >= watermarkData.length()) {
                    break outerLoop;
                }
            }
        }

        return watermarked;
    }

    private String sanitizeFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9-_]", "_");
    }
}
