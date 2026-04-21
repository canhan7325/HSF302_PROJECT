package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.bookmark.BookmarkRequest;
import com.group1.mangaflowweb.dto.bookmark.BookmarkResponse;
import com.group1.mangaflowweb.dto.view.BookmarkListItemView;
import com.group1.mangaflowweb.entity.ReadingHistories;
import com.group1.mangaflowweb.repository.ReadingHistoryRepository;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserContextService userContextService;
    private final ComicService comicService;
    private final ReadingHistoryRepository readingHistoryRepository;

    // ================== GET ALL ==================
    @GetMapping
    public String getAll(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer comicId,
            Model model) {

        Integer currentUserId = userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null);
        boolean isLoggedIn = currentUserId != null;

        final Integer effectiveUserId = (userId != null) ? userId : currentUserId;

        final List<BookmarkResponse> bookmarkResponses;
        if (effectiveUserId != null) {
            bookmarkResponses = bookmarkService.getByUserId(effectiveUserId);
        } else if (comicId != null) {
            bookmarkResponses = bookmarkService.getByComicId(comicId);
        } else {
            bookmarkResponses = bookmarkService.getAll();
        }

        final Map<Integer, ReadingHistories> latestByComic = (effectiveUserId != null)
                ? readingHistoryRepository.findByUser_UserIdOrderByReadAtDesc(effectiveUserId).stream()
                .filter(rh -> rh.getChapter() != null && rh.getChapter().getComic() != null)
                .collect(Collectors.toMap(
                        rh -> rh.getChapter().getComic().getComicId(),
                        Function.identity(),
                        // keep the first one (because list is sorted desc)
                        (a, b) -> a
                ))
                : Map.of();

        List<BookmarkListItemView> bookmarks = bookmarkResponses.stream()
                .map(br -> {
                    Integer bookmarkId = br.getBookmarkId();
                    Integer bComicId = br.getComicId();

                    var comic = (bComicId != null) ? comicService.getById(bComicId) : null;

                    ReadingHistories rh = (bComicId != null) ? latestByComic.get(bComicId) : null;
                    Integer continueChapterId = null;
                    Integer continueChapterNumber = null;
                    if (rh != null && rh.getChapter() != null) {
                        continueChapterId = rh.getChapter().getChapterId();
                        continueChapterNumber = rh.getChapter().getChapterNumber();
                    }

                    return BookmarkListItemView.builder()
                            .bookmarkId(bookmarkId)
                            .comicId(bComicId)
                            .comicName(comic != null ? comic.getTitle() : "")
                            .thumbnailUrl(comic != null ? comic.getCoverImg() : "")
                            .continueChapterId(continueChapterId)
                            .continueChapterNumber(continueChapterNumber)
                            .comicSlug(comic != null ? comic.getSlug() : null)
                            .bookmarked(true)
                            .build();
                })
                .sorted(Comparator.comparing(BookmarkListItemView::getBookmarkId, Comparator.nullsLast(Integer::compareTo)))
                .toList();

        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isLoggedIn", isLoggedIn);
        return "bookmark/list";
    }

    // ================== GET BY ID ==================
    @GetMapping("/{bookmarkId}")
    public String getById(@PathVariable Integer bookmarkId, Model model) {
        model.addAttribute("bookmark", bookmarkService.getById(bookmarkId));
        return "bookmark/detail"; // templates/bookmark/detail.html
    }

    // ================== FORM CREATE ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("bookmark", new BookmarkRequest());
        return "bookmark/create"; // templates/bookmark/create.html
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("bookmark") BookmarkRequest request) {
        bookmarkService.create(request);
        return "redirect:/bookmarks";
    }

    // ================== CREATE (AJAX by comicId for current user) ==================
    @PostMapping("/create-by-comic")
    @ResponseBody
    public Map<String, Object> createByComic(@RequestParam Integer comicId) {
        Integer currentUserId = userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null);
        if (currentUserId == null) {
            return Map.of("ok", false, "error", "UNAUTHORIZED");
        }

        // Avoid duplicates: if already bookmarked return ok
        boolean exists = bookmarkService.getByUserId(currentUserId).stream()
                .anyMatch(b -> b.getComicId() != null && b.getComicId().equals(comicId));
        if (!exists) {
            bookmarkService.create(BookmarkRequest.builder()
                    .userId(currentUserId)
                    .comicId(comicId)
                    .build());
        }

        return Map.of("ok", true, "bookmarked", true);
    }

    // ================== FORM UPDATE ==================
    @GetMapping("/edit/{bookmarkId}")
    public String showUpdateForm(@PathVariable Integer bookmarkId, Model model) {
        model.addAttribute("bookmark", bookmarkService.getById(bookmarkId));
        return "bookmark/edit"; // templates/bookmark/edit.html
    }

    // ================== UPDATE ==================
    @PostMapping("/edit/{bookmarkId}")
    public String update(@PathVariable Integer bookmarkId,
                         @Valid @ModelAttribute("bookmark") BookmarkRequest request) {
        bookmarkService.update(bookmarkId, request);
        return "redirect:/bookmarks";
    }

    // ================== DELETE ==================
    @GetMapping("/delete/{bookmarkId}")
    public String delete(@PathVariable Integer bookmarkId) {
        bookmarkService.delete(bookmarkId);
        return "redirect:/bookmarks";
    }

    @PostMapping("/toggle")
    @ResponseBody
    public Map<String, Object> toggle(@RequestParam Integer comicId) {
        Integer currentUserId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        if (currentUserId == null) {
            return Map.of("ok", false, "error", "UNAUTHORIZED");
        }

        // Find existing bookmark (if any)
        BookmarkResponse existing = bookmarkService.getByUserId(currentUserId).stream()
                .filter(b -> b.getComicId() != null && b.getComicId().equals(comicId))
                .findFirst()
                .orElse(null);

        if (existing == null) {
            BookmarkResponse created = bookmarkService.create(BookmarkRequest.builder()
                    .userId(currentUserId)
                    .comicId(comicId)
                    .build());
            return Map.of(
                    "ok", true,
                    "bookmarked", true,
                    "bookmarkId", created.getBookmarkId()
            );
        }

        bookmarkService.delete(existing.getBookmarkId());
        return Map.of(
                "ok", true,
                "bookmarked", false,
                "bookmarkId", existing.getBookmarkId()
        );
    }
}
