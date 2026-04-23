package com.group1.mangaflowweb.dto.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadingHistoryDTO {
    private Integer historyId;
    private LocalDateTime readingDate;
    private Integer userId;
    private Integer chapterId;
}
