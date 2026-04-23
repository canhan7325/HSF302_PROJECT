package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.service.AccessService;
import com.group1.mangaflowweb.service.ChapterPageService;
import com.group1.mangaflowweb.service.ChapterPdfService;
import com.group1.mangaflowweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

import javax.imageio.ImageIO;

@Service
@RequiredArgsConstructor
public class ChapterPdfServiceImpl implements ChapterPdfService {

    private final ChapterPageService chapterPageService;
    private final AccessService accessService;
    private final UserService userService;

    @Override
    public ResponseEntity<byte[]> generateChapterPdf(Long chapterId, Authentication auth) throws Exception {

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401)
                    .body("Bạn cần đăng nhập để tải PDF".getBytes());
        }

        Users user = userService.findEntityByUsername(auth.getName());
        if (user == null) {
            return ResponseEntity.status(401)
                    .body("User không tồn tại".getBytes());
        }

        String tier = accessService.getSubscriptionTier(user);
        if (!"silver".equals(tier) && !"gold".equals(tier)) {
            return ResponseEntity.status(403)
                    .body("Bạn cần gói Silver hoặc Gold để tải PDF".getBytes());
        }

        List<String> imageUrls = chapterPageService.getPageImageUrls(chapterId);

        String watermark = "©" + user.getUsername() + " " + OffsetDateTime.now();

        byte[] pdfBytes = buildPdf(imageUrls, watermark);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("chapter-" + chapterId + ".pdf")
                        .build()
        );

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    // ===================== PDF BUILDER =====================

    private byte[] buildPdf(List<String> imageUrls, String watermark) throws Exception {

        try (PDDocument doc = new PDDocument()) {

            for (String imgUrl : imageUrls) {

                BufferedImage img;
                try (InputStream in = new URL(imgUrl).openStream()) {
                    img = ImageIO.read(in);
                }

                if (img == null) continue;

                PDPage page = new PDPage(new PDRectangle(img.getWidth(), img.getHeight()));
                doc.addPage(page);

                PDImageXObject pdImage = PDImageXObject.createFromByteArray(
                        doc,
                        bufferedImageToBytes(img),
                        "page"
                );

                try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                    cs.drawImage(pdImage, 0, 0, img.getWidth(), img.getHeight());
                }

                // watermark
                try (PDPageContentStream cs = new PDPageContentStream(
                        doc,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )) {
                    cs.setNonStrokingColor(160, 160, 160);
                    cs.setFont(PDType1Font.HELVETICA_BOLD,
                            Math.max(18, img.getWidth() / 40f));

                    cs.beginText();
                    cs.setTextMatrix(
                            org.apache.pdfbox.util.Matrix.getRotateInstance(
                                    Math.toRadians(35),
                                    img.getWidth() * 0.15f,
                                    img.getHeight() * 0.15f
                            )
                    );
                    cs.showText(watermark);
                    cs.endText();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);
            return baos.toByteArray();
        }
    }

    private byte[] bufferedImageToBytes(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}