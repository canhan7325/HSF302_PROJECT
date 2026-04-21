package com.group1.mangaflowweb.dto.readinghistory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReadingHistoryRequest {
    Integer userId;
    Integer chapterId;
}
