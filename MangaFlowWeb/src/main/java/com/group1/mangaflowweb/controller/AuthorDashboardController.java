package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorDashboardController {

    private final UserContextService userContextService;
    private final ComicService comicService;

    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        Integer userId = userContextService.getCurrentUser().map(u -> u.getUserId()).orElse(null);
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
    public String uploadChapter() {
        return "author/upload-chapter";
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
