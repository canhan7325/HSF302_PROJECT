package com.group1.mangaflowweb.dto.request;

import lombok.Data;

@Data
public class GenreRequest {
    private Integer genreId;

    private String name;

    private String slug;
}
