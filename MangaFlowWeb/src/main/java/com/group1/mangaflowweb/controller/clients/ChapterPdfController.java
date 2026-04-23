package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.service.ChapterPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/chapters")
@RequiredArgsConstructor
public class ChapterPdfController {

    private final ChapterPdfService chapterPdfService;

    @GetMapping("/{chapterId}/pdf")
    public ResponseEntity<byte[]> downloadChapterPdf(@PathVariable Long chapterId,
                                                     Authentication auth) throws Exception {
        return chapterPdfService.generateChapterPdf(chapterId, auth);
    }
}