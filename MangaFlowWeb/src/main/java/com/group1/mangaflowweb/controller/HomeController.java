package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.dto.comic.ComicResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    @GetMapping({"/", "/index"})
    public String index(
            @RequestParam(name = "latestPage", defaultValue = "0") int latestPage,
            Model model) {

        // Fetch all comics and sort by viewCount for top comics
        var topComics = comicService.getAll().stream()
                .sorted(Comparator.comparingInt(comic -> -comic.getViewCount()))
                .limit(6)
                .collect(Collectors.toList());
        model.addAttribute("topComics", topComics);

        // Fetch all comics and sort by updatedAt (latest) with pagination
        var allComics = comicService.getAll().stream()
                .sorted(Comparator.comparing(ComicResponse::getUpdatedAt).reversed())
                .collect(Collectors.toList());

        int pageSize = 6;
        int start = latestPage * pageSize;
        int end = Math.min(start + pageSize, allComics.size());

        var pageContent = allComics.subList(start, end);
        Page<?> latestComicPage = new PageImpl<>(pageContent, PageRequest.of(latestPage, pageSize), allComics.size());

        model.addAttribute("latestComicPage", latestComicPage);

        return "index";
    }
}





