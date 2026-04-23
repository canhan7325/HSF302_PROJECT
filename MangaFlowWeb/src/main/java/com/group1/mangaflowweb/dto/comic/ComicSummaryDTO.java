package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComicSummaryDTO {
    private Integer comicId;
    private String title;
    private Integer viewCount;
    private ComicEnum status;
}
