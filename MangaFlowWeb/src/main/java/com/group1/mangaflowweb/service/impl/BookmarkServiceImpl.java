package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Bookmarks;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.BookmarkRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final ComicRepository comicRepository;
	private final UserRepository userRepository;

	@Override
	public boolean isBookmarked(Integer userId, Integer comicId) {
		return bookmarkRepository.existsByUser_UserIdAndComic_ComicId(userId, comicId);
	}

	@Override
	public long countFollowers(Integer comicId) {
		return bookmarkRepository.countByComic_ComicId(comicId);
	}

	@Override
	@Transactional
	public boolean toggleBookmark(Integer userId, Integer comicId) {
		return bookmarkRepository.findByUser_UserIdAndComic_ComicId(userId, comicId)
				.map(existing -> {
					bookmarkRepository.delete(existing);
					return false;
				})
				.orElseGet(() -> {
					Users user = userRepository.findById(userId)
							.orElseThrow(() -> new IllegalArgumentException("User not found"));
					Comics comic = comicRepository.findById(comicId)
							.orElseThrow(() -> new IllegalArgumentException("Comic not found"));

					Bookmarks bookmark = Bookmarks.builder()
							.user(user)
							.comic(comic)
							.build();
					bookmarkRepository.save(bookmark);
					return true;
				});
	}
    @Override
    public BookmarkResponse create(BookmarkRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));

        bookmarkRepository.findByUser_UserIdAndComic_ComicId(request.getUserId(), request.getComicId())
                .ifPresent(bookmark -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Bookmark already exists for this user and comic");
                });

        Bookmarks bookmark = Bookmarks.builder()
                .user(user)
                .comic(comic)
                .build();

        return toResponse(bookmarkRepository.save(bookmark));
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkResponse getById(Integer bookmarkId) {
        return toResponse(findBookmarkOrThrow(bookmarkId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getAll() {
        return bookmarkRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getByUserId(Integer userId) {
        return bookmarkRepository.findByUser_UserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkResponse> getByComicId(Integer comicId) {
        return bookmarkRepository.findByComic_ComicId(comicId).stream().map(this::toResponse).toList();
    }

    @Override
    public BookmarkResponse update(Integer bookmarkId, BookmarkRequest request) {
        Bookmarks bookmark = findBookmarkOrThrow(bookmarkId);

        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));

        bookmarkRepository.findByUser_UserIdAndComic_ComicId(request.getUserId(), request.getComicId())
                .filter(existing -> !existing.getBookmarkId().equals(bookmarkId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Bookmark already exists for this user and comic");
                });

        bookmark.setUser(user);
        bookmark.setComic(comic);
        return toResponse(bookmarkRepository.save(bookmark));
    }

    @Override
    public void delete(Integer bookmarkId) {
        Bookmarks bookmark = findBookmarkOrThrow(bookmarkId);
        bookmarkRepository.delete(bookmark);
    }

    private Bookmarks findBookmarkOrThrow(Integer bookmarkId) {
        return bookmarkRepository.findById(bookmarkId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Bookmark not found"));
    }

    private BookmarkResponse toResponse(Bookmarks bookmark) {
        return BookmarkResponse.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .userId(bookmark.getUser() != null ? bookmark.getUser().getUserId() : null)
                .comicId(bookmark.getComic() != null ? bookmark.getComic().getComicId() : null)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }
}
