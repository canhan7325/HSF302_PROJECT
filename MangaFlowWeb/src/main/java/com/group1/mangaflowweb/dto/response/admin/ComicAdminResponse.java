package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicAdminResponse {
    private Integer comicId;
    private String title;
    private String slug;
    private ComicEnum status;
    private Integer viewCount;
    private int chapterCount;
    private String uploaderUsername;
    private String coverImg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Integer> genreIds;
}
