package com.group1.mangaflowweb.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreComicsId implements Serializable {

    @Column(name = "comic_id")
    private Integer comicId;

    @Column(name = "genre_id")
    private Integer genreId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenreComicsId that = (GenreComicsId) o;
        return Objects.equals(comicId, that.comicId) && Objects.equals(genreId, that.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(comicId, genreId);
    }
}

