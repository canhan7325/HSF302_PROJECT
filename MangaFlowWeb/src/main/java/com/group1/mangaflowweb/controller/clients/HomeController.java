package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.comic.ComicDTO;
import com.group1.mangaflowweb.service.ComicService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ComicService comicService;

    @GetMapping({ "/", "/index" })
    public String index(
            @RequestParam(name = "latestPage", defaultValue = "0") int latestPage,
            Model model) {

        // Fetch all comics and sort by viewCount for top comics
        var topComics = comicService.getAll().stream()
                .sorted(Comparator.comparingInt(comic -> -comic.getViewCount()))
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("topComics", topComics);

        // Fetch all comics and sort by latest chapter's createdAt, falling back to comic's updatedAt with pagination
        var allComics = comicService.getAll().stream()
                .sorted(Comparator.comparing((ComicDTO comic) -> {
                    if (comic.getChapters() != null && !comic.getChapters().isEmpty()) {
                        java.time.LocalDateTime latestChapterTime = comic.getChapters().get(comic.getChapters().size() - 1).getCreatedAt();
                        if (latestChapterTime != null) {
                            return latestChapterTime;
                        }
                    }
                    return comic.getUpdatedAt() != null ? comic.getUpdatedAt() : java.time.LocalDateTime.MIN;
                }).reversed())
                .collect(Collectors.toList());

        int pageSize = 6;
        int start = latestPage * pageSize;
        int end = Math.min(start + pageSize, allComics.size());

        var pageContent = allComics.subList(start, end);
        Page<?> latestComicPage = new PageImpl<>(pageContent, PageRequest.of(latestPage, pageSize), allComics.size());

        model.addAttribute("latestComicPage", latestComicPage);

        return "clients/home/index";
    }

    @GetMapping("/search-comic")
    public String searchComic(@RequestParam(name = "q", required = false) String query, Model model) {
        String keyword = query == null ? "" : query.trim();
        var results = comicService.searchForPageByTitle(keyword);

        model.addAttribute("query", keyword);
        model.addAttribute("searchResults", results);
        return "clients/comics/search-comic";
    }
}



