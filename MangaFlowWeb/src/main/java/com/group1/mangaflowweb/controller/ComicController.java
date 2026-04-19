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

@Controller
public class ComicController {

    @Autowired
    private ComicRepository comicRepository;

    @GetMapping({"/", "/index"})
    public String index(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "12") int size) {

        // Cấu hình phân trang, mỗi trang 12 truyện
        Pageable pageable = PageRequest.of(page, size);
        Page<Comics> comicPage = comicRepository.findAll(pageable);

        model.addAttribute("comicPage", comicPage);
        return "index";
    }
}
