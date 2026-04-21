package com.group1.mangaflowweb.dto.response.admin;

import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicSummaryResponse {
    private Integer comicId;
    private String title;
    private Integer viewCount;
    private ComicEnum status;
}
