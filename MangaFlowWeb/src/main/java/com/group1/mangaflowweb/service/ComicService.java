package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.response.ComicDetailResponse;

public interface ComicService {
	ComicDetailResponse getComicDetail(String slug, String username);
}
