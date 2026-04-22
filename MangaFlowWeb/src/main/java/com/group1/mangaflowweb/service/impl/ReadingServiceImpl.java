package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.ChapterReadViewDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;
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

        ChapterResponse chapter = chapterService.getById(chapterId);
        Integer comicId = chapter.getComicId();
        var comic = comicService.getById(comicId);
        List<PageResponse> pages = chapterService.getAllPageByChapterId(chapterId);


        readingHistoryService.incrementComicViewCount(comicId);

        var currentUserOpt = userContextService.getCurrentUser();
        Integer currentUserId = currentUserOpt
                .map(com.group1.mangaflowweb.entity.Users::getUserId)
                .orElse(null);

        if (currentUserId != null) {
            readingHistoryService.upsertForUserReadChapter(currentUserId, chapterId);
        }


        List<ChapterResponse> chaptersInComic = (comicId != null)
                ? chapterService.getByComicId(comicId)
                : List.of();

        chaptersInComic = chaptersInComic.stream()
                .sorted(Comparator.comparing(ChapterResponse::getChapterNumber))
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


        AccessService.ChapterAccess access = currentUserOpt
                .map(accessService::getChapterAccess)
                .orElse(new AccessService.ChapterAccess(false, 2));

        boolean canReadFull = access.isCanReadFull();
        int previewCount = access.getPreviewCount();


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
                .build();
    }
}
