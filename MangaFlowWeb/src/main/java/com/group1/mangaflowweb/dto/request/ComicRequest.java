package com.group1.mangaflowweb.dto.request;

import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.enums.ComicEnum;
import lombok.Data;

import java.util.List;

@Data
public class ComicRequest {
    private String comicId;

    private String title;

    private String slug;

    private String description;

    private String coverImg;

    private ComicEnum status;

    private Users user;

    private List<ChapterRequest> chapterRequests;

    private List<GenreRequest> genreRequests;
}
