package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.comic.ComicSummaryDTO;
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
public class ComicRestController {

    private final ComicService comicService;

    @GetMapping("/search")
    public List<ComicSummaryDTO> searchByTitle(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "q", required = false) String legacyQuery) {
        String keyword = (query != null && !query.isBlank()) ? query : legacyQuery;
        return comicService.searchByTitle(keyword);
    }
}



