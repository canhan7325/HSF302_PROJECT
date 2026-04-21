package com.group1.mangaflowweb.dto.readinghistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReadingHistoryResponse {
    private Integer historyId;
    private LocalDateTime readingDate;
    private Integer userId;
    private Integer chapterId;
}
