package com.group1.mangaflowweb.dto.chapter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChapterDTO {
    @NotNull
    private Integer comicId;

    @NotNull
    private Integer chapterNumber;

    private String title;
}
