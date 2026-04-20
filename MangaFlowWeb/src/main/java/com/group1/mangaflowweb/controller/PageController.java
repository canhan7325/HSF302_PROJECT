package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;
import com.group1.mangaflowweb.service.PageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/pages")
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    // ================== GET ALL ==================
    @GetMapping
    public String getAll(@RequestParam(required = false) Integer chapterId,
                         Model model) {

        List<?> pages = (chapterId != null)
                ? pageService.getByChapterId(chapterId)
                : pageService.getAll();

        model.addAttribute("pages", pages);
        return "page/list"; // templates/page/list.html
    }

    // ================== GET BY ID ==================
    @GetMapping("/{pageId}")
    public String getById(@PathVariable Integer pageId, Model model) {
        model.addAttribute("page", pageService.getById(pageId));
        return "page/detail"; // templates/page/detail.html
    }

    // ================== FORM CREATE ==================
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("page", new PageRequest());
        return "page/create"; // templates/page/create.html
    }

    // ================== CREATE ==================
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("page") PageRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            return "page/create";
        }

        try {
            pageService.create(request);
            return "redirect:/pages";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "page/create";
        }
    }

    // ================== FORM UPDATE ==================
    @GetMapping("/edit/{pageId}")
    public String showUpdateForm(@PathVariable Integer pageId, Model model) {
        model.addAttribute("page", pageService.getById(pageId));
        return "page/edit"; // templates/page/edit.html
    }

    // ================== UPDATE ==================
    @PostMapping("/edit/{pageId}")
    public String update(@PathVariable Integer pageId,
                         @Valid @ModelAttribute("page") PageRequest request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            PageResponse current = pageService.getById(pageId);
            model.addAttribute("page", current);
            return "page/edit";
        }

        try {
            pageService.update(pageId, request);
            return "redirect:/pages";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            PageResponse current = pageService.getById(pageId);
            model.addAttribute("page", current);
            return "page/edit";
        }
    }

    // ================== DELETE ==================
    @GetMapping("/delete/{pageId}")
    public String delete(@PathVariable Integer pageId) {
        pageService.delete(pageId);
        return "redirect:/pages";
    }
}