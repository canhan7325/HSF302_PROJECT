package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.chapter.ChapterReadViewDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.page.PageDTO;
import com.group1.mangaflowweb.service.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReadingServiceImpl implements ReadingService{
    private final ChapterService chapterService;
    private final ComicService comicService;
    private final BookmarkService bookmarkService;
    private final ReadingHistoryService readingHistoryService;
    private final AccessService accessService;
    private final UserContextService userContextService;

    @Override
    @Transactional
    public ChapterReadViewDTO getChapterReadDetails(Integer chapterId) {

        ChapterDTO chapter = chapterService.getById(chapterId);
        Integer comicId = chapter.getComicId();
        var comic = comicService.getById(comicId);
        List<PageDTO> pages = chapterService.getAllPageByChapterId(chapterId);


        readingHistoryService.incrementComicViewCount(comicId);

        var currentUserOpt = userContextService.getCurrentUser();
        Integer currentUserId = currentUserOpt
                .map(com.group1.mangaflowweb.entity.Users::getUserId)
                .orElse(null);

        if (currentUserId != null) {
            readingHistoryService.upsertForUserReadChapter(currentUserId, chapterId);
        }


        List<ChapterDTO> chaptersInComic = (comicId != null)
                ? chapterService.getByComicId(comicId)
                : List.of();

        chaptersInComic = chaptersInComic.stream()
                .sorted(Comparator.comparing(ChapterDTO::getChapterNumber))
                .toList();

        Integer prevChapterId = null;
        Integer nextChapterId = null;
        for (int i = 0; i < chaptersInComic.size(); i++) {
            if (chaptersInComic.get(i).getChapterId().equals(chapterId)) {
                if (i > 0) prevChapterId = chaptersInComic.get(i - 1).getChapterId();
                if (i < chaptersInComic.size() - 1) nextChapterId = chaptersInComic.get(i + 1).getChapterId();
                break;
            }
        }


        String tier = currentUserOpt
                .map(accessService::getSubscriptionTier)
                .orElse("none");

        boolean canReadFull = false;
        int chapNum = chapter.getChapterNumber() != null ? chapter.getChapterNumber() : 0;

        if (chapNum == 1) {
            canReadFull = true;
        } else if (chapNum <= 10) {
            canReadFull = "silver".equals(tier) || "gold".equals(tier);
        } else {
            canReadFull = "gold".equals(tier);
        }

        int previewCount = 0; // No longer used for page-level preview


        boolean isOwner = currentUserId != null && comic.getUserId() != null
                && comic.getUserId().equals(currentUserId);

        if (isOwner) {
            canReadFull = true;
        }

        boolean isBookmarked = (currentUserId != null && comicId != null)
                && bookmarkService.isBookmarked(currentUserId, comicId);


        return ChapterReadViewDTO.builder()
                .chapter(chapter)
                .pages(pages)
                .comicId(comicId)
                .comicTitle(comic.getTitle())
                .comicSlug(comic.getSlug())
                .chaptersInComic(chaptersInComic)
                .prevChapterId(prevChapterId)
                .nextChapterId(nextChapterId)
                .canReadFull(canReadFull)
                .previewCount(previewCount)
                .isOwner(isOwner)
                .isBookmarked(isBookmarked)
                .tier(tier)
                .build();
    }
}


