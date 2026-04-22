package com.group1.mangaflowweb.dto.request;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreDTO {
    private Integer genreId;

    private String name;
}
