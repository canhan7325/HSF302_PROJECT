package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class PageAdController {

    private final PageService pageService;
    private final ComicService comicService;
    private final ChapterService chapterService;

    public PageAdController(PageService pageService, ComicService comicService, ChapterService chapterService) {
        this.pageService = pageService;
        this.comicService = comicService;
        this.chapterService = chapterService;
    }

    @GetMapping("/manga/{id}/chapters/{chId}/pages")
    public String chapterPages(@PathVariable Integer id, @PathVariable Integer chId, Model model) {
        model.addAttribute("comic", comicService.getComicById(id));
        model.addAttribute("chapter", chapterService.getChapterById(chId));
        model.addAttribute("pages", pageService.getPagesByChapter(chId));
        model.addAttribute("comicId", id);
        model.addAttribute("chapterId", chId);
        model.addAttribute("view", "pages");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @GetMapping("/manga/{id}/chapters/{chId}/pages/json")
    @ResponseBody
    public List<PageAdminResponse> getPagesJson(@PathVariable Integer id, @PathVariable Integer chId) {
        return pageService.getPagesByChapter(chId);
    }

    @PostMapping("/manga/{id}/chapters/{chId}/pages/add")
    public String pageAdd(@PathVariable Integer id, @PathVariable Integer chId,
                          @RequestParam("file") List<MultipartFile> files,
                          RedirectAttributes redirectAttributes) {
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                pageService.uploadAndAddPage(chId, file);
            }
        }
        redirectAttributes.addFlashAttribute("successMessage", "Pages uploaded.");
        return "redirect:/admin/manga/" + id + "/chapters/" + chId + "/pages";
    }

    @PostMapping("/manga/{id}/chapters/{chId}/pages/{pageId}/delete")
    public String pageDelete(@PathVariable Integer id, @PathVariable Integer chId,
                             @PathVariable Integer pageId,
                             RedirectAttributes redirectAttributes) {
        pageService.deletePage(pageId);
        redirectAttributes.addFlashAttribute("successMessage", "Page deleted.");
        return "redirect:/admin/manga/" + id + "/chapters/" + chId + "/pages";
    }

    @PostMapping("/manga/{id}/chapters/{chId}/pages/reorder")
    @ResponseBody
    public ResponseEntity<String> pageReorder(@PathVariable Integer id, @PathVariable Integer chId,
                                              @RequestBody List<Integer> pageIds) {
        pageService.reorderPages(chId, pageIds);
        return ResponseEntity.ok("ok");
    }
}
