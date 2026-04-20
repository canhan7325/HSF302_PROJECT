package com.group1.mangaflowweb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "pages", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "chapter_id", "page_number" })
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Pages {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "page_id")
    private Integer pageId;

    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    @Column(name = "img_path", nullable = false, length = 500)
    private String imgPath;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapters chapter;
}
