package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.bookmark.BookmarkRequest;
import com.group1.mangaflowweb.dto.bookmark.BookmarkResponse;
import com.group1.mangaflowweb.dto.view.BookmarkListItemView;

import java.util.List;
import java.util.Map;

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

    List<BookmarkListItemView> getUserBookmarkListView(Integer userId, Integer comicId);
    Map<String, Object> toggleBookmarkStatus(Integer comicId);
}
