package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.bookmark.BookmarkRequest;
import com.group1.mangaflowweb.dto.bookmark.BookmarkResponse;

import java.util.List;

public interface BookmarkService {
	boolean isBookmarked(Integer userId, Integer comicId);

	long countFollowers(Integer comicId);

	boolean toggleBookmark(Integer userId, Integer comicId);
    BookmarkResponse create(BookmarkRequest request);

    BookmarkResponse getById(Integer bookmarkId);

    List<BookmarkResponse> getAll();

    List<BookmarkResponse> getByUserId(Integer userId);

    List<BookmarkResponse> getByComicId(Integer comicId);

    BookmarkResponse update(Integer bookmarkId, BookmarkRequest request);

    void delete(Integer bookmarkId);
}
