package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.bookmark.BookmarkDTO;
import com.group1.mangaflowweb.dto.bookmark.BookmarkListItemDTO;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService bookmarkService;
    private final UserContextService userContextService;

    @GetMapping
    public String getAll(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer comicId,
            Model model) {

        Integer currentUserId = userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null);
        final Integer effectiveUserId = (userId != null) ? userId : currentUserId;

        List<BookmarkListItemDTO> bookmarks = bookmarkService.getUserBookmarkListView(effectiveUserId, comicId);

        model.addAttribute("bookmarks", bookmarks);
        model.addAttribute("currentUserId", currentUserId);
        model.addAttribute("isLoggedIn", currentUserId != null);
        return "clients/bookmark/list";
    }

    // ================== GET BY ID ==================
    @GetMapping("/{bookmarkId}")
    public String getById(@PathVariable Integer bookmarkId, Model model) {
        model.addAttribute("bookmark", bookmarkService.getById(bookmarkId));
        return "clients/bookmark/detail"; // templates/bookmark/detail.html
    }

    // ================== FORM CREATE ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("bookmark", new BookmarkDTO());
        return "clients/bookmark/create"; // templates/bookmark/create.html
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
        return "clients/bookmark/edit"; // templates/bookmark/edit.html
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



