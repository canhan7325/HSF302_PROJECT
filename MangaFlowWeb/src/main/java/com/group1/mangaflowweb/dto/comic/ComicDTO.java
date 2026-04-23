package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ComicDTO {
    private Integer comicId;

    @NotBlank
    private String title;

    @NotBlank
    private String slug;

    private String description;

    private String coverImg;

    @NotNull
    private ComicEnum status;

    private Integer viewCount;
    private Integer followerCount;
    private Boolean bookmarked;
    private Integer userId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<Integer> genreIds;

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
