package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.page.PageDTO;
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

    @GetMapping
    public String getAll(@RequestParam(required = false) Integer chapterId,
                         Model model) {

        List<PageDTO> pages = (chapterId != null)
                ? pageService.getByChapterId(chapterId)
                : pageService.getAll();

        model.addAttribute("pages", pages);
        return "clients/page/list"; // templates/page/list.html
    }

   @GetMapping("/{pageId}")
    public String getById(@PathVariable Integer pageId, Model model) {
        model.addAttribute("page", pageService.getById(pageId));
        return "clients/page/detail"; // templates/page/detail.html
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("page", new PageDTO());
        return "clients/page/create"; // templates/page/create.html
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("page") PageDTO request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            return "clients/page/create";
        }

        try {
            pageService.create(request);
            return "redirect:/pages";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            return "clients/page/create";
        }
    }

   @GetMapping("/edit/{pageId}")
    public String showUpdateForm(@PathVariable Integer pageId, Model model) {
        model.addAttribute("page", pageService.getById(pageId));
        return "clients/page/edit"; // templates/page/edit.html
    }

    @PostMapping("/edit/{pageId}")
    public String update(@PathVariable Integer pageId,
                         @Valid @ModelAttribute("page") PageDTO request,
                         BindingResult bindingResult,
                         Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("error", "Please fill all required fields correctly.");
            PageDTO current = pageService.getById(pageId);
            model.addAttribute("page", current);
            return "clients/page/edit";
        }

        try {
            pageService.update(pageId, request);
            return "redirect:/pages";
        } catch (ResponseStatusException ex) {
            model.addAttribute("error", ex.getReason());
            PageDTO current = pageService.getById(pageId);
            model.addAttribute("page", current);
            return "clients/page/edit";
        }
    }

    @GetMapping("/delete/{pageId}")
    public String delete(@PathVariable Integer pageId) {
        pageService.delete(pageId);
        return "redirect:/pages";
    }
}

