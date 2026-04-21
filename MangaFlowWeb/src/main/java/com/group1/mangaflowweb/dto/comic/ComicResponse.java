package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicResponse {
    private Integer comicId;
    private String title;
    private String slug;
    private String description;
    private String coverImg;
    private ComicEnum status;
    private Integer viewCount;
    private Integer followerCount;
    private Boolean bookmarked;
    private Integer userId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Builder.Default
    private List<ChapterSummary> chapters = new ArrayList<>();

    @Builder.Default
    private List<GenreSummary> genres = new ArrayList<>();

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChapterSummary {
        private Integer chapterId;
        private Integer chapterNumber;
        private String title;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GenreSummary {
        private Integer genreId;
        private String name;
    }
}
