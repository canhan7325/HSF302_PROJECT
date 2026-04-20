package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.response.PageAdminResponse;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.PageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class PageController {

    private final PageService pageService;
    private final ComicService comicService;
    private final ChapterService chapterService;

    public PageController(PageService pageService, ComicService comicService, ChapterService chapterService) {
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
    @ResponseBody
    public ResponseEntity<String> pageAdd(@PathVariable Integer id, @PathVariable Integer chId,
                                          @RequestParam MultipartFile file) {
        String imgPath = pageService.uploadAndAddPage(chId, file);
        return ResponseEntity.ok(imgPath);
    }

    @PostMapping("/manga/{id}/chapters/{chId}/pages/{pageId}/delete")
    @ResponseBody
    public ResponseEntity<String> pageDelete(@PathVariable Integer id, @PathVariable Integer chId,
                                             @PathVariable Integer pageId) {
        pageService.deletePage(pageId);
        return ResponseEntity.ok("ok");
    }

    @PostMapping("/manga/{id}/chapters/{chId}/pages/reorder")
    @ResponseBody
    public ResponseEntity<String> pageReorder(@PathVariable Integer id, @PathVariable Integer chId,
                                              @RequestBody List<Integer> pageIds) {
        pageService.reorderPages(chId, pageIds);
        return ResponseEntity.ok("ok");
    }
}
