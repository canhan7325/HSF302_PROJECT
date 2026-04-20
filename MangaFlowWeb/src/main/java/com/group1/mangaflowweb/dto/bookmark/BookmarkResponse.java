package com.group1.mangaflowweb.dto.bookmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookmarkResponse {
    private Integer bookmarkId;
    private Integer userId;
    private Integer comicId;
    private LocalDateTime createdAt;
}
