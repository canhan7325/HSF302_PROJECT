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
    private Integer comicId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, unique = true, length = 255)
    private String slug;

    @Column(columnDefinition = "VARCHAR(MAX)")
    private String description;

    @Column(length = 500)
    private String coverImg;

    @Enumerated(EnumType.STRING)
    private ComicEnum status = ComicEnum.ONGOING;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
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

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "genre_comics",
            joinColumns = @JoinColumn(name = "comic_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private List<Genres> genres = new ArrayList<>();
}


