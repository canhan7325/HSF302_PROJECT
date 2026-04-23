package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.history.ReadingHistoryDTO;

import com.group1.mangaflowweb.entity.ReadingHistories;
import java.util.List;

public interface ReadingHistoryService {
    ReadingHistoryDTO readingHistory(ReadingHistoryDTO readingHistoryRequest);
    void upsertForUserReadChapter(Integer userId, Integer chapterId);
    Integer resolveReadNowChapterId(Integer userId, Integer comicId);
    
    List<ReadingHistories> findByUserIdOrderByReadAtDesc(Integer userId);

    void incrementComicViewCount(Integer comicId);
}

