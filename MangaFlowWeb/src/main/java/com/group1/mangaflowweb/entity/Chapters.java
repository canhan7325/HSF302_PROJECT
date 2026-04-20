package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chapters", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "comic_id", "chapter_number" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Chapters {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Integer chapterId;

    @Column(name = "chapter_number", nullable = false)
    private Integer chapterNumber;

    @Column(length = 255)
    private String title;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comic_id", nullable = false)
    private Comics comic;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Pages> pages = new ArrayList<>();

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReadingHistories> readingHistories = new ArrayList<>();
}
