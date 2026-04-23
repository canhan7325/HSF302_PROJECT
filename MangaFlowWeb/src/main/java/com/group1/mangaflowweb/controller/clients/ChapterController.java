package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.chapter.ChapterReadViewDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
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
    private final ReadingService readingService;

    // ================== GET ALL ==================
    @GetMapping
    public String getAll(@RequestParam(required = false) Integer comicId,
                         Model model) {

        Object chapters = (comicId != null)
                ? chapterService.getByComicId(comicId)
                : chapterService.getAll();

        model.addAttribute("chapters", chapters);
        return "clients/chapter/list";
    }

    // ================== GET BY ID ==================
    @GetMapping("/{chapterId}")
    public String getById(@PathVariable Integer chapterId, Model model) {
        model.addAttribute("chapter", chapterService.getById(chapterId));
        return "clients/chapter/detail";
    }

    // ================== FORM CREATE ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("chapter", new ChapterDTO());
        return "clients/chapter/create";
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("chapter") ChapterDTO request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            return "clients/chapter/create";
        }

        try {
            chapterService.create(request);
            return "redirect:/chapters";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "clients/chapter/create";
        }
    }

    // ================== FORM UPDATE ==================
    @GetMapping("/edit/{chapterId}")
    public String showUpdateForm(@PathVariable Integer chapterId, Model model) {
        model.addAttribute("chapter", chapterService.getById(chapterId));
        return "clients/chapter/edit";
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
            ChapterDTO current = chapterService.getById(chapterId);
            model.addAttribute("chapter", current);
            return "clients/chapter/edit";
        }

        try {
            chapterService.update(chapterId, request);
            return "redirect:/chapters";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            // repopulate chapterId for the form action
            ChapterDTO current = chapterService.getById(chapterId);
            model.addAttribute("chapter", current);
            return "clients/chapter/edit";
        }
    }

    // ================== DELETE ==================
    @GetMapping("/delete/{chapterId}")
    public String delete(@PathVariable Integer chapterId) {
        chapterService.delete(chapterId);
        return "redirect:/chapters";
    }

    // ================== READ CHAPTER (QUAN TRá»ŒNG) ==================
    @GetMapping("/{chapterId}/read")
    public String readChapter(@PathVariable Integer chapterId, Model model) {
        ChapterReadViewDTO readView = readingService.getChapterReadDetails(chapterId);

        model.addAttribute("readView", readView);
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

        boolean isLoggedIn = userContextService.getCurrentUser().isPresent();
        model.addAttribute("isLoggedIn", isLoggedIn);

        return "clients/chapter/read";
    }
}


