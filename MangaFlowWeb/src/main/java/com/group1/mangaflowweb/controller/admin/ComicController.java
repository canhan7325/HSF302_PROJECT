package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.request.admin.ComicRequest;
import com.group1.mangaflowweb.service.ComicService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ComicController {

    private final ComicService comicService;

    public ComicController(ComicService comicService) {
        this.comicService = comicService;
    }

    @GetMapping("/manga")
    public String mangaList(@PageableDefault(size = 12) Pageable pageable,
                            @RequestParam(required = false) String search,
                            Model model) {
        model.addAttribute("page", comicService.getComicsPage(pageable, search));
        model.addAttribute("search", search);
        model.addAttribute("view", "list");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @GetMapping("/manga/new")
    public String mangaNewForm(Model model) {
        model.addAttribute("comic", new ComicRequest());
        model.addAttribute("genres", comicService.getAllGenresWithCount());
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @PostMapping("/manga/new")
    public String mangaCreate(@Valid ComicRequest comic, BindingResult result,
                              Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genres", comicService.getAllGenresWithCount());
            model.addAttribute("view", "form");
            model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
            return "admin/manga";
        }
        comicService.createComic(comic);
        redirectAttributes.addFlashAttribute("successMessage", "Manga created successfully.");
        return "redirect:/admin/manga";
    }

    @GetMapping("/manga/{id}/edit")
    public String mangaEditForm(@PathVariable Integer id, Model model) {
        var existing = comicService.getComicById(id);
        ComicRequest form = new ComicRequest();
        form.setTitle(existing.title());
        form.setDescription(null);
        form.setCoverImg(null);
        form.setStatus(existing.status());
        model.addAttribute("comic", form);
        model.addAttribute("comicInfo", existing);
        model.addAttribute("genres", comicService.getAllGenresWithCount());
        model.addAttribute("editMode", true);
        model.addAttribute("comicId", id);
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @PostMapping("/manga/{id}/edit")
    public String mangaUpdate(@PathVariable Integer id,
                              @Valid ComicRequest comic, BindingResult result,
                              Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genres", comicService.getAllGenresWithCount());
            model.addAttribute("editMode", true);
            model.addAttribute("comicId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
            return "admin/manga";
        }
        comicService.updateComic(id, comic);
        redirectAttributes.addFlashAttribute("successMessage", "Manga updated successfully.");
        return "redirect:/admin/manga";
    }

    @PostMapping("/manga/{id}/delete")
    public String mangaDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        comicService.softDeleteComic(id);
        redirectAttributes.addFlashAttribute("successMessage", "Manga deleted.");
        return "redirect:/admin/manga";
    }
}
