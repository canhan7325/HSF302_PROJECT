package com.group1.mangaflowweb.dto.chapter;

import com.group1.mangaflowweb.dto.page.PageDTO;
import jakarta.validation.constraints.NotNull;
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
public class ChapterDTO {
    private Integer chapterId;

    @NotNull
    private Integer comicId;

    @NotNull
    private Integer chapterNumber;

    private String title;

    private LocalDateTime createdAt;

    private List<PageDTO> pages;
}
