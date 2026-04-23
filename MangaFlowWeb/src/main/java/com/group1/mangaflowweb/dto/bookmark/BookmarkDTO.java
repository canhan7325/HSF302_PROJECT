package com.group1.mangaflowweb.dto.bookmark;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkDTO {
    private Integer bookmarkId;

    @NotNull
    private Integer userId;

    @NotNull
    private Integer comicId;

    private LocalDateTime createdAt;
}
