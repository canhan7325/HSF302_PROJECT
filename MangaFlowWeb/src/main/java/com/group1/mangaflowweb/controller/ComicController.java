package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ComicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ComicController {

    @Autowired
    private ComicRepository comicRepository;

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
}
