package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryDTO;
import com.group1.mangaflowweb.dto.readinghistory.ReadingHistoryResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.ReadingHistories;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.ReadingHistoryRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.ReadingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReadingHistoryServiceImpl implements ReadingHistoryService {
    private final ReadingHistoryRepository readingHistoryRepository;
    private final UserRepository userRepository;
    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;

    @Override
    public ReadingHistoryResponse readingHistory(ReadingHistoryDTO ReadingHistoryDTO) {
        Users user = userRepository.findById(ReadingHistoryDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Chapters chapter = chapterRepository.findById(ReadingHistoryDTO.getChapterId())
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

    @Override
    @Transactional
    public void upsertForUserReadChapter(Integer userId, Integer chapterId) {
        if (userId == null || chapterId == null) {
            return;
        }

        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));
        if (chapter.getComic() == null || chapter.getComic().getComicId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chapter missing comic");
        }

        Integer comicId = chapter.getComic().getComicId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ReadingHistories rh = readingHistoryRepository
                .findFirstByUser_UserIdAndChapter_Comic_ComicIdOrderByReadAtDesc(userId, comicId)
                .orElse(null);

        if (rh == null) {
            // create new
            readingHistoryRepository.save(ReadingHistories.builder()
                    .user(user)
                    .chapter(chapter)
                    .readAt(LocalDateTime.now())
                    .build());
            return;
        }

        // update existing record to new chapter for same comic
        rh.setChapter(chapter);
        rh.setReadAt(LocalDateTime.now());
        readingHistoryRepository.save(rh);
    }

    @Override
    @Transactional(readOnly = true)
    public Integer resolveReadNowChapterId(Integer userId, Integer comicId) {
        if (comicId == null) {
            return null;
        }

        if (userId != null) {
            Optional<ReadingHistories> lastHistory = readingHistoryRepository
                    .findFirstByUser_UserIdAndChapter_Comic_ComicIdOrderByReadAtDesc(userId, comicId);

            if (lastHistory.isPresent() && lastHistory.get().getChapter() != null) {
                return lastHistory.get().getChapter().getChapterId();
            }
        }

        return chapterRepository.findFirstByComic_ComicIdOrderByChapterNumberAsc(comicId)
                .map(Chapters::getChapterId)
                .orElse(null);
    }

    @Override
    @Transactional
    public void incrementComicViewCount(Integer comicId) {
        if (comicId == null)
            return;

        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));

        Integer current = comic.getViewCount() == null ? 0 : comic.getViewCount();
        comic.setViewCount(current + 1);
        comicRepository.save(comic);
    }

    @Override
    public java.util.List<ReadingHistories> findByUserIdOrderByReadAtDesc(Integer userId) {
        return readingHistoryRepository.findByUser_UserIdOrderByReadAtDesc(userId);
    }
}
