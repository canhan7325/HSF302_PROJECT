package com.group1.mangaflowweb.dto.comic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicSearchResponse {
    private Integer comicId;
    private String title;
    private String slug;
    private String coverImg;
}
