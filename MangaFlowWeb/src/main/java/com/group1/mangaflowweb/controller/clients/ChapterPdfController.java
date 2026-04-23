package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.service.AccessService;
import com.group1.mangaflowweb.service.ChapterPageService;
import com.group1.mangaflowweb.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterPdfController {

    private final ChapterPageService chapterPageService;
    private final AccessService accessService;
    private final UserService userService;

    @GetMapping("/{chapterId}/pdf")
    public ResponseEntity<byte[]> downloadChapterPdf(@PathVariable Long chapterId, Authentication auth) throws Exception {

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

        String username = (auth != null && auth.getName() != null) ? auth.getName() : "guest";
        String watermark = "©" + username + OffsetDateTime.now();

        List<String> imageUrls = chapterPageService.getPageImageUrls(chapterId);

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

                try (PDPageContentStream cs = new PDPageContentStream(
                        doc,
                        page,
                        PDPageContentStream.AppendMode.APPEND,
                        true,
                        true
                )) {
                    cs.setNonStrokingColor(160, 160, 160);
                    cs.setFont(PDType1Font.HELVETICA_BOLD, Math.max(18, img.getWidth() / 40f));

                    cs.beginText();
                    cs.setTextMatrix(org.apache.pdfbox.util.Matrix.getRotateInstance(
                            Math.toRadians(35),
                            img.getWidth() * 0.15f,
                            img.getHeight() * 0.15f
                    ));
                    cs.showText(watermark);
                    cs.endText();
                }
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            doc.save(baos);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.attachment()
                            .filename("chapter-" + chapterId + ".pdf")
                            .build()
            );

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(baos.toByteArray());
        }
    }

    private byte[] bufferedImageToBytes(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }
}