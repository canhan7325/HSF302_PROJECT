package com.group1.mangaflowweb.dto.comic;

import com.group1.mangaflowweb.enums.ComicEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComicRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String slug;

    private String description;

    private String coverImg;

    @NotNull
    private ComicEnum status;

    @NotNull
    private Integer userId;
}
