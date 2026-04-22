package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.ChapterReadViewDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final UserContextService userContextService;
    private final AccessService accessService;
    private final ComicService comicService;
    private final BookmarkService bookmarkService;
    private final ReadingHistoryService readingHistoryService;
    private final ReadingService readingService;

    // ================== GET ALL ==================
    @GetMapping
    public String getAll(@RequestParam(required = false) Integer comicId,
                         Model model) {

        Object chapters = (comicId != null)
                ? chapterService.getByComicId(comicId)
                : chapterService.getAll();

        model.addAttribute("chapters", chapters);
        return "chapter/list";
    }

    // ================== GET BY ID ==================
    @GetMapping("/{chapterId}")
    public String getById(@PathVariable Integer chapterId, Model model) {
        model.addAttribute("chapter", chapterService.getById(chapterId));
        return "chapter/detail";
    }

    // ================== FORM CREATE ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("chapter", new ChapterDTO());
        return "chapter/create";
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("chapter") ChapterDTO request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            return "chapter/create";
        }

        try {
            chapterService.create(request);
            return "redirect:/chapters";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "chapter/create";
        }
    }

    // ================== FORM UPDATE ==================
    @GetMapping("/edit/{chapterId}")
    public String showUpdateForm(@PathVariable Integer chapterId, Model model) {
        model.addAttribute("chapter", chapterService.getById(chapterId));
        return "chapter/edit";
    }

    // ================== UPDATE ==================
    @PostMapping("/edit/{chapterId}")
    public String update(@PathVariable Integer chapterId,
                         @Valid @ModelAttribute("chapter") ChapterDTO request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            // keep chapterId visible for edit.html action
            ChapterResponse current = chapterService.getById(chapterId);
            model.addAttribute("chapter", current);
            return "chapter/edit";
        }

        try {
            chapterService.update(chapterId, request);
            return "redirect:/chapters";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            // repopulate chapterId for the form action
            ChapterResponse current = chapterService.getById(chapterId);
            model.addAttribute("chapter", current);
            return "chapter/edit";
        }
    }

    // ================== DELETE ==================
    @GetMapping("/delete/{chapterId}")
    public String delete(@PathVariable Integer chapterId) {
        chapterService.delete(chapterId);
        return "redirect:/chapters";
    }

    // ================== READ CHAPTER (QUAN TRỌNG) ==================
    @GetMapping("/{chapterId}/read")
    public String readChapter(@PathVariable Integer chapterId, Model model) {
        ChapterReadViewDTO readView = readingService.getChapterReadDetails(chapterId);

        model.addAttribute("readView", readView); // Bạn có thể add nguyên object
        // Hoặc nếu file HTML cũ dùng các biến lẻ, bạn add từng cái:
        model.addAttribute("chapter", readView.getChapter());
        model.addAttribute("pages", readView.getPages());
        model.addAttribute("comicId", readView.getComicId());
        model.addAttribute("comicTitle", readView.getComicTitle());
        model.addAttribute("comicSlug", readView.getComicSlug());
        model.addAttribute("chaptersInComic", readView.getChaptersInComic());
        model.addAttribute("prevChapterId", readView.getPrevChapterId());
        model.addAttribute("nextChapterId", readView.getNextChapterId());
        model.addAttribute("canReadFull", readView.isCanReadFull());
        model.addAttribute("previewCount", readView.getPreviewCount());
        model.addAttribute("isBookmarked", readView.isBookmarked());

        // isLoggedIn và currentUserId có thể lấy từ userContextService trực tiếp ở Controller nếu cần
        boolean isLoggedIn = userContextService.getCurrentUser().isPresent();
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "chapter/read";
    }
}