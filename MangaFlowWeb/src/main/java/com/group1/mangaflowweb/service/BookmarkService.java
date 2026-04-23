package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.bookmark.BookmarkDTO;
import com.group1.mangaflowweb.dto.bookmark.BookmarkListItemDTO;

import java.util.List;
import java.util.Map;

public interface BookmarkService {
    boolean isBookmarked(Integer userId, Integer comicId);

    long countFollowers(Integer comicId);

    boolean toggleBookmark(Integer userId, Integer comicId);

    BookmarkDTO create(BookmarkDTO request);

    BookmarkDTO getById(Integer bookmarkId);

    List<BookmarkDTO> getAll();

    List<BookmarkDTO> getByUserId(Integer userId);

    List<BookmarkDTO> getByComicId(Integer comicId);

    BookmarkDTO update(Integer bookmarkId, BookmarkDTO request);

    void delete(Integer bookmarkId);

    List<BookmarkListItemDTO> getUserBookmarkListView(Integer userId, Integer comicId);

    Map<String, Object> toggleBookmarkStatus(Integer comicId);
}

