package com.group1.mangaflowweb.dto.page;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    @NotNull
    private Integer chapterId;

    @NotNull
    private Integer pageNumber;

    @NotNull
    private String imgPath;
}
