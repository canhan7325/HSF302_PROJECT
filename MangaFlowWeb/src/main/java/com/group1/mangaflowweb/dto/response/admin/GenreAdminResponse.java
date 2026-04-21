package com.group1.mangaflowweb.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreAdminResponse {
    private Integer genreId;
    private String name;
    private String slug;
    private long comicCount;
}
