package com.group1.mangaflowweb.dto.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkListItemView {
    private Integer bookmarkId;
    private Integer comicId;
    private String comicName;
    private String thumbnailUrl;
    private Integer continueChapterId;
    private Integer continueChapterNumber;
}
