package com.group1.mangaflowweb.dto.chapter;

import com.group1.mangaflowweb.dto.page.PageResponse;
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
public class ChapterResponse {
    private Integer chapterId;
    private Integer comicId;
    private Integer chapterNumber;
    private String title;
    private LocalDateTime createdAt;

    // optional: include pages when needed (e.g., chapter detail/read)
    private List<PageResponse> pages;
}
