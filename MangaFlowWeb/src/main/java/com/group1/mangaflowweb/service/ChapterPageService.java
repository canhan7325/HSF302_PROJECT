package com.group1.mangaflowweb.service;

import java.util.List;

public interface ChapterPageService {
    List<String> getPageImageUrls(Long chapterId);
}