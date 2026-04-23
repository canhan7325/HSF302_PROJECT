package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.request.admin.GenreAdRequest;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;
import com.group1.mangaflowweb.service.GenreService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class GenreAdController {

    private final GenreService genreService;

    public GenreAdController(GenreService genreService) {
        this.genreService = genreService;
    }

    @GetMapping("/genres")
    public String genreList(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("genres", genreService.searchGenres(search));
        } else {
            model.addAttribute("genres", genreService.getAllGenresWithCount());
        }
        model.addAttribute("search", search);
        model.addAttribute("view", "list");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/genres";
    }

    @GetMapping("/genres/new")
    public String genreNewForm(Model model) {
        model.addAttribute("genre", new GenreAdRequest());
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/genres";
    }

    @PostMapping("/genres/new")
    public String genreCreate(@Valid GenreAdRequest genre, BindingResult result,
                              Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors()) {
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/genres";
        }
        try {
            genreService.createGenre(genre);
        } catch (IllegalArgumentException e) {
            model.addAttribute("duplicateError", e.getMessage());
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/genres";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Genre created successfully.");
        return "redirect:/admin/genres";
    }

    @GetMapping("/genres/{id}/edit")
    public String genreEditForm(@PathVariable Integer id, Model model) {
        GenreAdminResponse existing = genreService.getAllGenresWithCount().stream()
                .filter(g -> g.getGenreId().equals(id))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
        model.addAttribute("genreInfo", existing);
        model.addAttribute("editMode", true);
        model.addAttribute("genreId", id);
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/genres";
    }

    @PostMapping("/genres/{id}/edit")
    public String genreUpdate(@PathVariable Integer id,
                              @Valid GenreAdRequest genre, BindingResult result,
                              Model model, RedirectAttributes redirectAttributes) {
        String usernameCtx = SecurityContextHolder.getContext().getAuthentication().getName();
        if (result.hasErrors()) {
            model.addAttribute("editMode", true);
            model.addAttribute("genreId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/genres";
        }
        try {
            genreService.updateGenre(id, genre);
        } catch (IllegalArgumentException e) {
            model.addAttribute("duplicateError", e.getMessage());
            model.addAttribute("editMode", true);
            model.addAttribute("genreId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", usernameCtx);
            return "admin/genres";
        }
        redirectAttributes.addFlashAttribute("successMessage", "Genre updated successfully.");
        return "redirect:/admin/genres";
    }

    @PostMapping("/genres/{id}/delete")
    public String genreDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            genreService.deleteGenre(id);
            redirectAttributes.addFlashAttribute("successMessage", "Genre deleted.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/genres";
    }
}
