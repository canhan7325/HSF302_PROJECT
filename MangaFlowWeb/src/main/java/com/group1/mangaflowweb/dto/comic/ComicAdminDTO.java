package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import jakarta.validation.constraints.NotBlank;
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
public class ComicAdminDTO {
    private Integer comicId;
    @NotBlank
    private String title;

    private String slug;
    private String description;
    private String coverImg;
    private ComicEnum status;
    private Integer viewCount;
    private int chapterCount;
    private String uploaderUsername;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Integer> genreIds;
}
