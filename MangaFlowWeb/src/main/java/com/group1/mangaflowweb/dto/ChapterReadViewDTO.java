package com.group1.mangaflowweb.dto;

import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterReadViewDTO {
    private ChapterResponse chapter;
    private List<PageResponse> pages;

    private Integer comicId;
    private String comicTitle;
    private String comicSlug;

    private List<ChapterResponse> chaptersInComic;
    private Integer prevChapterId;
    private Integer nextChapterId;

    private boolean canReadFull;
    private int previewCount;
    private boolean isOwner;

    private boolean isBookmarked;
}
