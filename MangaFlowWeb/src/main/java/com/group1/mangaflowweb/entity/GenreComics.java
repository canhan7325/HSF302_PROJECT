package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "genre_comics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GenreComics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genres genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comics comic;
}