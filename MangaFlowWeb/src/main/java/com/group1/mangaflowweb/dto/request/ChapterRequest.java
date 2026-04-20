package com.group1.mangaflowweb.dto.request;

import com.group1.mangaflowweb.entity.Comics;
import lombok.Data;

import java.util.List;

@Data
public class ChapterRequest {
    private Integer chapterId;

    private Integer chapterNumber;

    private String title;

    private Comics comic;

    private List<PageRequest> pageRequests;
}
