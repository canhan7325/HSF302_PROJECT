package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.ComicRequest;
import com.group1.mangaflowweb.dto.response.ComicDetailResponse;

public interface ComicService {
    void addComic(ComicRequest comicRequest);
	ComicDetailResponse getComicDetail(String slug, String username);
}
