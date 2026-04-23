package com.group1.mangaflowweb.dto.chapter;

import com.group1.mangaflowweb.dto.page.PageAdminDTO;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChapterAdminDTO {
    private Integer chapterId;

    @NotNull
    private Integer chapterNumber;

    @NotBlank
    private String title;

    private int pageCount;

    private LocalDateTime createdAt;

    private List<PageAdminDTO> pages;
}
