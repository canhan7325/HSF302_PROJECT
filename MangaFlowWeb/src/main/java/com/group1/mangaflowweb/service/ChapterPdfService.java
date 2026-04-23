package com.group1.mangaflowweb.service;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
public interface ChapterPdfService {
    ResponseEntity<byte[]> generateChapterPdf(Long chapterId, Authentication auth) throws Exception;
}
