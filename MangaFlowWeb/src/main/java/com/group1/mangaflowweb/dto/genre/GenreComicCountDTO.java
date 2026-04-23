package com.group1.mangaflowweb.dto.genre;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenreComicCountDTO {
    private Integer genreId;
    private String genreName;
    private long comicCount;
}
