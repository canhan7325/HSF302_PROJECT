package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.comic.ComicResponse;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.PageService;
import com.group1.mangaflowweb.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorDashboardController {
    private final UserContextService userContextService;
    private final ComicService comicService;
    private final ChapterService chapterService;
    private final PageService pageService;

    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        Integer userId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        model.addAttribute("currentUserId", userId);

        if (userId == null) {
            model.addAttribute("error", "Please login to access Author Studio.");
            model.addAttribute("myComics", java.util.List.of());
            return "author/dashboard";
        }

        model.addAttribute("myComics", comicService.getByUserId(userId));
        return "author/dashboard";
    }


    @GetMapping("/upload-chapter")
    public String uploadChapterForm(Model model) {
        Integer userId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        model.addAttribute("currentUserId", userId);
        if (userId == null) {
            model.addAttribute("error", "Please login to access Author Studio.");
            model.addAttribute("myComics", java.util.List.of());
            return "author/upload-chapter";
        }

        model.addAttribute("myComics", comicService.getByUserId(userId));
        return "author/upload-chapter";
    }

    @PostMapping("/upload-chapter")
    public String handleUploadChapter(@RequestParam Integer comicId,
                                      @RequestParam Integer chapterNumber,
                                      @RequestParam(required = false, name = "chapterTitle") String chapterTitle,
                                      @RequestParam(name = "pages") MultipartFile[] pages,
                                      RedirectAttributes redirectAttributes) {
        Integer currentUserId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to upload chapters.");
            return "redirect:/author/upload-chapter";
        }

        // Authorization: ensure selected comic belongs to current user
        ComicResponse comic = comicService.getById(comicId);
        if (comic.getUserId() == null || !comic.getUserId().equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "You don't have permission to upload chapters for this series.");
            return "redirect:/author/upload-chapter";
        }

        if (pages == null || pages.length == 0) {
            redirectAttributes.addFlashAttribute("error", "Please upload at least 1 page image.");
            return "redirect:/author/upload-chapter";
        }

        ChapterDTO req = new ChapterDTO(
                comicId,
                chapterNumber,
                (chapterTitle != null && !chapterTitle.isBlank()) ? chapterTitle.trim() : null
        );

        Integer chapterId;
        try {
            chapterId = chapterService.create(req).getChapterId();
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return "redirect:/author/upload-chapter";
        }

        int savedCount = 0;
        for (MultipartFile file : pages) {
            if (file == null || file.isEmpty()) continue;
            try {
                pageService.uploadAndAddPage(chapterId, file);
                savedCount++;
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error",
                        "Uploaded chapter but failed to save some pages. Last error: " + ex.getMessage());
                return "redirect:/chapters/" + chapterId + "/read";
            }
        }

        redirectAttributes.addFlashAttribute("message", "Chapter uploaded successfully (" + savedCount + " pages)." );
        return "redirect:/chapters/" + chapterId + "/read";
    }


    @GetMapping("/analytics")
    public String analytics() {
        return "author/dashboard";
    }

    @GetMapping("/settings")
    public String settings() {
        return "author/dashboard";
    }
}
