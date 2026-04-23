package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.chapter.ChapterReadViewDTO;

public interface ReadingService {
    ChapterReadViewDTO getChapterReadDetails(Integer chapterId);
}

