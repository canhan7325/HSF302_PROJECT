package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.bookmark.BookmarkDTO;
import com.group1.mangaflowweb.dto.bookmark.BookmarkResponse;
import com.group1.mangaflowweb.dto.view.BookmarkListItemView;
import com.group1.mangaflowweb.entity.ReadingHistories;
import com.group1.mangaflowweb.repository.ChapterRepository;
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
    private final ChapterRepository chapterRepository;

    @GetMapping
    public String getAll(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer comicId,
            Model model) {

        Integer currentUserId = userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null);
        final Integer effectiveUserId = (userId != null) ? userId : currentUserId;

        List<BookmarkListItemView> bookmarks = bookmarkService.getUserBookmarkListView(effectiveUserId, comicId);

        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isLoggedIn", currentUserId != null);
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
        model.addAttribute("bookmark", new BookmarkDTO());
        return "bookmark/create"; // templates/bookmark/create.html
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("bookmark") BookmarkDTO request) {
        bookmarkService.create(request);
        return "redirect:/bookmarks";
    }

    // ================== CREATE (AJAX by comicId for current user) ==================
    @PostMapping("/create-by-comic")
    @ResponseBody
    public Map<String, Object> createByComic(@RequestParam Integer comicId) {
        return bookmarkService.toggleBookmarkStatus(comicId);
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
                         @Valid @ModelAttribute("bookmark") BookmarkDTO request) {
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
        return bookmarkService.toggleBookmarkStatus(comicId);
    }
}
