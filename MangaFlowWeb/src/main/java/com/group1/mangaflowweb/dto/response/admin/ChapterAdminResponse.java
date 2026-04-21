package com.group1.mangaflowweb.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterAdminResponse {
    private Integer chapterId;
    private Integer chapterNumber;
    private String title;
    private int pageCount;
    private LocalDateTime createdAt;
}
