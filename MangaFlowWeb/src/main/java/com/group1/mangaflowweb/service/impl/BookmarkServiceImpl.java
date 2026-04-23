package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.bookmark.BookmarkDTO;
import com.group1.mangaflowweb.dto.bookmark.BookmarkListItemDTO;
import com.group1.mangaflowweb.entity.*;
import com.group1.mangaflowweb.repository.*;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

    private final BookmarkRepository bookmarkRepository;
    private final ComicRepository comicRepository;
    private final UsersRepository usersRepository;
    private final ReadingHistoryRepository readingHistoryRepository;
    private final ChapterRepository chapterRepository;
    private final UserContextService userContextService;
    private final ComicService comicService;

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
                    Users user = usersRepository.findById(userId)
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
    public BookmarkDTO create(BookmarkDTO request) {
        Users user = usersRepository.findById(request.getUserId())
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

        return toDTO(bookmarkRepository.save(bookmark));
    }

    @Override
    @Transactional(readOnly = true)
    public BookmarkDTO getById(Integer bookmarkId) {
        return toDTO(findBookmarkOrThrow(bookmarkId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDTO> getAll() {
        return bookmarkRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDTO> getByUserId(Integer userId) {
        return bookmarkRepository.findByUser_UserId(userId).stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkDTO> getByComicId(Integer comicId) {
        return bookmarkRepository.findByComic_ComicId(comicId).stream().map(this::toDTO).toList();
    }

    @Override
    public BookmarkDTO update(Integer bookmarkId, BookmarkDTO request) {
        Bookmarks bookmark = findBookmarkOrThrow(bookmarkId);

        Users user = usersRepository.findById(request.getUserId())
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
        return toDTO(bookmarkRepository.save(bookmark));
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

    private BookmarkDTO toDTO(Bookmarks bookmark) {
        return BookmarkDTO.builder()
                .bookmarkId(bookmark.getBookmarkId())
                .userId(bookmark.getUser() != null ? bookmark.getUser().getUserId() : null)
                .comicId(bookmark.getComic() != null ? bookmark.getComic().getComicId() : null)
                .createdAt(bookmark.getCreatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookmarkListItemDTO> getUserBookmarkListView(Integer userId, Integer comicId) {

        List<BookmarkDTO> bookmarkDTOs;
        if (userId != null) {
            bookmarkDTOs = getByUserId(userId);
        } else if (comicId != null) {
            bookmarkDTOs = getByComicId(comicId);
        } else {
            bookmarkDTOs = getAll();
        }

        final Map<Integer, ReadingHistories> latestByComic = (userId != null)
                ? readingHistoryRepository.findByUser_UserIdOrderByReadAtDesc(userId).stream()
                .filter(rh -> rh.getChapter() != null && rh.getChapter().getComic() != null)
                .collect(Collectors.toMap(
                        rh -> rh.getChapter().getComic().getComicId(),
                        Function.identity(),
                        (a, b) -> a
                ))
                : Map.of();

        return bookmarkDTOs.stream()
                .map(br -> {
                    Integer bComicId = br.getComicId();
                    var comic = (bComicId != null) ? comicService.getById(bComicId) : null;

                    ReadingHistories rh = (bComicId != null) ? latestByComic.get(bComicId) : null;
                    Integer continueChapterId = (rh != null && rh.getChapter() != null) ? rh.getChapter().getChapterId() : null;
                    Integer continueChapterNumber = (rh != null && rh.getChapter() != null) ? rh.getChapter().getChapterNumber() : null;

                    Chapters first = null;
                    if (bComicId != null) {
                        first = chapterRepository
                                .findFirstByComic_ComicIdOrderByChapterNumberAsc(bComicId)
                                .orElse(null);
                    }

                    Integer firstChapterId = (first != null) ? first.getChapterId() : null;
                    Integer firstChapterNumber = (first != null) ? first.getChapterNumber() : null;

                    return BookmarkListItemDTO.builder()
                            .bookmarkId(br.getBookmarkId())
                            .comicId(bComicId)
                            .comicName(comic != null ? comic.getTitle() : "")
                            .thumbnailUrl(comic != null ? comic.getCoverImg() : "")
                            .continueChapterId(continueChapterId)
                            .continueChapterNumber(continueChapterNumber)
                            .firstChapterId(firstChapterId)
                            .firstChapterNumber(firstChapterNumber)
                            .comicSlug(comic != null ? comic.getSlug() : null)
                            .bookmarked(true)
                            .build();
                })
                .sorted(Comparator.comparing(BookmarkListItemDTO::getBookmarkId, Comparator.nullsLast(Integer::compareTo)))
                .toList();
    }

    @Override
    @Transactional
    public Map<String, Object> toggleBookmarkStatus(Integer comicId) {
        Integer currentUserId = userContextService.getCurrentUser()
                .map(Users::getUserId)
                .orElse(null);

        if (currentUserId == null) {
            return Map.of("ok", false, "error", "UNAUTHORIZED");
        }

        boolean isNowBookmarked = toggleBookmark(currentUserId, comicId);

        return Map.of(
                "ok", true,
                "bookmarked", isNowBookmarked
        );
    }
}

