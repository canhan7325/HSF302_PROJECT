package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.chapter.ChapterRequest;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;
import com.group1.mangaflowweb.service.AccessService;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;
    private final UserContextService userContextService;
    private final AccessService accessService;
    private final ComicService comicService;

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
        model.addAttribute("chapter", new ChapterRequest());
        return "chapter/create";
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("chapter") ChapterRequest request,
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
                         @Valid @ModelAttribute("chapter") ChapterRequest request,
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
        ChapterResponse chapter = chapterService.getById(chapterId);
        model.addAttribute("chapter", chapter);

        List<PageResponse> pages = chapterService.getAllPageByChapterId(chapterId);
        model.addAttribute("pages", pages);

        Integer comicId = chapter.getComicId();
        model.addAttribute("comicId", comicId);
        String comicTitle = comicService.getById(comicId).getTitle();
        model.addAttribute("comicTitle", comicTitle);

        // Chapters dropdown + prev/next navigation (by chapterNumber)
        List<ChapterResponse> chaptersInComic = (comicId != null)
                ? chapterService.getByComicId(comicId)
                : List.of();
        chaptersInComic = chaptersInComic.stream()
                .sorted(Comparator.comparing(ChapterResponse::getChapterNumber))
                .toList();
        model.addAttribute("chaptersInComic", chaptersInComic);

        Integer prevChapterId = null;
        Integer nextChapterId = null;
        for (int i = 0; i < chaptersInComic.size(); i++) {
            if (chaptersInComic.get(i).getChapterId().equals(chapterId)) {
                if (i > 0) prevChapterId = chaptersInComic.get(i - 1).getChapterId();
                if (i < chaptersInComic.size() - 1) nextChapterId = chaptersInComic.get(i + 1).getChapterId();
                break;
            }
        }
        model.addAttribute("prevChapterId", prevChapterId);
        model.addAttribute("nextChapterId", nextChapterId);

        boolean isLoggedIn = userContextService.getCurrentUser().isPresent();
        boolean canReadFull = userContextService.getCurrentUser()
                .map(accessService::canReadFullChapter)
                .orElse(false);

        model.addAttribute("isLoggedIn", isLoggedIn);
        model.addAttribute("canReadFull", canReadFull);
        model.addAttribute("previewCount", 2);


        // for bookmark (if logged in)
        model.addAttribute("currentUserId", userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null));

        return "chapter/read";
    }
}