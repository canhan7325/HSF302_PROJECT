package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.stereotype.Service;

@Service
public interface ChapterService {
    void addChapter(ChapterRequest chapterRequest, Comics comic);
}
