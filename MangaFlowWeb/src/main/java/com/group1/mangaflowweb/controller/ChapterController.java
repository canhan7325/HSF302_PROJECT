package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ChapterController {

    private final ChapterService chapterService;
    private final ComicService comicService;

    public ChapterController(ChapterService chapterService, ComicService comicService) {
        this.chapterService = chapterService;
        this.comicService = comicService;
    }

    @GetMapping("/manga/{id}/chapters")
    public String chapterList(@PathVariable Integer id, Model model) {
        model.addAttribute("comic",    comicService.getComicById(id));
        model.addAttribute("chapters", chapterService.getChaptersByComic(id));
        model.addAttribute("chapter",  new ChapterRequest());
        model.addAttribute("comicId",  id);
        model.addAttribute("view",     "chapters");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @PostMapping("/manga/{id}/chapters/new")
    public String chapterCreate(@PathVariable Integer id,
                                @Valid ChapterRequest chapter, BindingResult result,
                                Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("comic",    comicService.getComicById(id));
            model.addAttribute("chapters", chapterService.getChaptersByComic(id));
            model.addAttribute("comicId",  id);
            model.addAttribute("view",     "chapters");
            model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
            return "admin/manga";
        }
        chapterService.createChapter(id, chapter);
        redirectAttributes.addFlashAttribute("successMessage", "Chapter added.");
        return "redirect:/admin/manga/" + id + "/chapters";
    }

    @PostMapping("/manga/{id}/chapters/{chId}/edit")
    public String chapterUpdate(@PathVariable Integer id, @PathVariable Integer chId,
                                @Valid ChapterRequest chapter, BindingResult result,
                                RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            chapterService.updateChapter(chId, chapter);
            redirectAttributes.addFlashAttribute("successMessage", "Chapter updated.");
        }
        return "redirect:/admin/manga/" + id + "/chapters";
    }

    @PostMapping("/manga/{id}/chapters/{chId}/delete")
    public String chapterDelete(@PathVariable Integer id, @PathVariable Integer chId,
                                RedirectAttributes redirectAttributes) {
        chapterService.deleteChapter(chId);
        redirectAttributes.addFlashAttribute("successMessage", "Chapter deleted.");
        return "redirect:/admin/manga/" + id + "/chapters";
    }
}
