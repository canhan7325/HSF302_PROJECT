package com.group1.mangaflowweb.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ComicDetailResponse {
    private Integer comicId;
    private String title;
    private String slug;
    private String authorName;
    private String description;
    private String coverImg;
    private Integer viewCount;
    private long followerCount;
    private boolean bookmarked;
    private List<String> genres;
    private List<ChapterItem> chapters;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ChapterItem {
        private Integer chapterId;
        private Integer chapterNumber;
        private String title;
        private String createdAt;
    }
}

