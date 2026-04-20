package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.response.ComicDetailResponse;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.ComicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
public class ComicController {

    @Autowired
    private ComicRepository comicRepository;

    @Autowired
    private ComicService comicService;

    @Autowired
    private BookmarkService bookmarkService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping({"/", "/index"})
    public String index(Model model,
                        @RequestParam(defaultValue = "0") int latestPage) {

        int safeLatestPage = Math.max(latestPage, 0);
        Pageable latestPageable = PageRequest.of(safeLatestPage, 12);
        List<Comics> topComics = comicRepository.findTop6ByOrderByViewCountDesc();
        Page<Comics> latestComicPage = comicRepository.findAllByOrderByUpdatedAtDesc(latestPageable);

        model.addAttribute("topComics", topComics);
        model.addAttribute("latestComicPage", latestComicPage);
        return "index";
    }

    @GetMapping("/comic/{slug}")
    public String comicDetail(@PathVariable String slug,
                              Principal principal,
                              Model model) {
        try {
            String username = principal != null ? principal.getName() : null;
            ComicDetailResponse comic = comicService.getComicDetail(slug, username);
            model.addAttribute("comic", comic);
            model.addAttribute("hasChapters", comic.getChapters() != null && !comic.getChapters().isEmpty());
            return "comic-detail";
        } catch (IllegalArgumentException ex) {
            return "redirect:/index";
        }
    }

    @PostMapping("/comic/{slug}/bookmark")
    public String toggleBookmark(@PathVariable String slug,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        return userRepository.findByUsername(principal.getName())
                .flatMap(user -> comicRepository.findBySlug(slug)
                        .map(comic -> {
                            boolean bookmarked = bookmarkService.toggleBookmark(user.getUserId(), comic.getComicId());
                            redirectAttributes.addFlashAttribute("bookmarkMessage",
                                    bookmarked ? "Da them vao thu vien." : "Da bo theo doi truyen.");
                            return "redirect:/comic/" + slug;
                        }))
                .orElse("redirect:/index");
    }
}
