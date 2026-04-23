package com.group1.mangaflowweb.dto.page;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Integer pageId;

    @NotNull
    private Integer chapterId;

    @NotNull
    private Integer pageNumber;

    @NotNull
    private String imgPath;
}
