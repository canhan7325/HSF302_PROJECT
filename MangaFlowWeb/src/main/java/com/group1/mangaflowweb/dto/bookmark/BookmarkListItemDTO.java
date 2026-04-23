package com.group1.mangaflowweb.dto.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkListItemDTO {
    private Integer bookmarkId;
    private Integer comicId;
    private String comicName;
    private String thumbnailUrl;
    private Integer continueChapterId;
    private Integer continueChapterNumber;
    private Integer firstChapterId;
    private Integer firstChapterNumber;
    // for UI actions
    private String comicSlug;
    private Boolean bookmarked;
}
