package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryRequest;
import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.ReadingHistories;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ReadingHistoryRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.ReadingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {
    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;


    @Override
    public ReadingHistoryResponse readingHistory(ReadingHistoryRequest readingHistoryRequest) {
        Users user = userRepository.findById(readingHistoryRequest.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chapters chapter = chapterRepository.findById(readingHistoryRequest.getChapterId())
                .orElseThrow(() -> new RuntimeException("Chapter not found"));

        ReadingHistories readingHistories = ReadingHistories.builder()
                .user(user)
                .chapter(chapter)
                .readAt(LocalDateTime.now())
                .build();
        return toResponse(readingHistoryRepository.save(readingHistories));
    }

    private ReadingHistoryResponse toResponse(ReadingHistories readingHistories) {
        return ReadingHistoryResponse.builder()
                .historyId(readingHistories.getReadingHistoryId())
                .readingDate(readingHistories.getReadAt())
                .userId(readingHistories.getUser().getUserId())
                .chapterId(readingHistories.getChapter().getChapterId())
                .build();
    }
}
