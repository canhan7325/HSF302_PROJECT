package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryRequest;
import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryResponse;

public interface ReadingHistoryService {
    ReadingHistoryResponse readingHistory(ReadingHistoryRequest readingHistoryRequest);
    void upsertForUserReadChapter(Integer userId, Integer chapterId);
    Integer resolveReadNowChapterId(Integer userId, Integer comicId);

    void incrementComicViewCount(Integer comicId);
}
