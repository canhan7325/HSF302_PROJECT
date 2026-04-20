package com.group1.mangaflowweb.service;

public interface BookmarkService {
	boolean isBookmarked(Integer userId, Integer comicId);

	long countFollowers(Integer comicId);

	boolean toggleBookmark(Integer userId, Integer comicId);
}
