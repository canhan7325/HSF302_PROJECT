package com.group1.mangaflowweb.dto.chapter;

import com.group1.mangaflowweb.dto.page.PageDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChapterReadViewDTO {
    private ChapterDTO chapter;
    private List<PageDTO> pages;

    private Integer comicId;
    private String comicTitle;
    private String comicSlug;

    private List<ChapterDTO> chaptersInComic;
    private Integer prevChapterId;
    private Integer nextChapterId;

    private boolean canReadFull;
    private int previewCount;
    private boolean isOwner;

    private boolean isBookmarked;
}

