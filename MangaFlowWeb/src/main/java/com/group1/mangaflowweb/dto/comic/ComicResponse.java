package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    private Integer userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
