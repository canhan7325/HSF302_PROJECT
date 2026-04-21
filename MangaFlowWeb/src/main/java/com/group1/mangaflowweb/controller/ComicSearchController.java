package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.comic.ComicSearchResponse;
import com.group1.mangaflowweb.service.ComicService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/comics")
@RequiredArgsConstructor
public class ComicSearchController {

    private final ComicService comicService;

    @GetMapping("/search")
    public List<ComicSearchResponse> searchByTitle(
            @RequestParam(name = "q", required = false) String keyword,
            @RequestParam(name = "limit", defaultValue = "8") int limit
    ) {
        return comicService.searchByTitle(keyword, limit);
    }
}
