package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;
import com.group1.mangaflowweb.enums.ComicEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comics")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Comics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comic_id")
    private Integer comicId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "VARCHAR(MAX)")
    private String description;

    @Column(name = "cover_img", length = 500)
    private String coverImg;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ComicEnum status = ComicEnum.ONGOING;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Chapters> chapters = new ArrayList<>();

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Bookmarks> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "comic", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GenreComics> genreComics = new ArrayList<>();
}
