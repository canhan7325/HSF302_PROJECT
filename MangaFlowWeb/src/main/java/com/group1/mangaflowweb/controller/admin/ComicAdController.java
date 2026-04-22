package com.group1.mangaflowweb.controller.admin;

import com.group1.mangaflowweb.dto.request.admin.ComicAdDTO;
import com.group1.mangaflowweb.service.CloudinaryUploadService;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ComicAdController {

    private final ComicService comicService;
    private final CloudinaryUploadService cloudinaryUploadService;

    public ComicAdController(ComicService comicService, CloudinaryUploadService cloudinaryUploadService) {
        this.comicService = comicService;
        this.cloudinaryUploadService = cloudinaryUploadService;
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
        model.addAttribute("comic", new ComicAdDTO());
        model.addAttribute("genres", comicService.getAllGenresWithCount());
        model.addAttribute("view", "form");
        model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
        return "admin/manga";
    }

    @PostMapping("/manga/new")
    public String mangaCreate(@Valid ComicAdDTO comic, BindingResult result,
                              @RequestParam(required = false) MultipartFile coverFile,
                              Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genres", comicService.getAllGenresWithCount());
            model.addAttribute("view", "form");
            model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
            return "admin/manga";
        }
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                // Use a temp slug from the title for the public ID
                String slug = comic.getTitle() != null
                        ? comic.getTitle().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "")
                        : "cover";
                String storedId = cloudinaryUploadService.uploadImage(coverFile, "comics/" + slug + "/cover");
                comic.setCoverImg(storedId);
            } catch (Exception e) {
                // proceed without cover
            }
        }
        comicService.createComic(comic);
        redirectAttributes.addFlashAttribute("successMessage", "Manga created successfully.");
        return "redirect:/admin/manga";
    }

    @GetMapping("/manga/{id}/edit")
    public String mangaEditForm(@PathVariable Integer id, Model model) {
        var existing = comicService.getComicById(id);
        ComicAdDTO form = new ComicAdDTO();
        form.setTitle(existing.getTitle());
        form.setDescription(null);
        form.setCoverImg(null);
        form.setStatus(existing.getStatus());
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
                              @Valid ComicAdDTO comic, BindingResult result,
                              @RequestParam(required = false) MultipartFile coverFile,
                              Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("genres", comicService.getAllGenresWithCount());
            model.addAttribute("editMode", true);
            model.addAttribute("comicId", id);
            model.addAttribute("view", "form");
            model.addAttribute("username", SecurityContextHolder.getContext().getAuthentication().getName());
            return "admin/manga";
        }
        // Upload new cover if provided
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String storedId = cloudinaryUploadService.uploadImage(coverFile, "comics/" + id + "/cover");
                comic.setCoverImg(storedId);
            } catch (Exception e) {
                // keep existing coverImg from hidden field
            }
        }
        comicService.updateComic(id, comic);
        redirectAttributes.addFlashAttribute("successMessage", "Manga updated successfully.");
        return "redirect:/admin/manga";
    }

    @PostMapping("/manga/{id}/delete")
    public String mangaDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        comicService.softDeleteComic(id);
        redirectAttributes.addFlashAttribute("successMessage", "Manga disabled.");
        return "redirect:/admin/manga";
    }

    @PostMapping("/manga/{id}/hard-delete")
    public String mangaHardDelete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        comicService.hardDeleteComic(id);
        redirectAttributes.addFlashAttribute("successMessage", "Manga permanently deleted.");
        return "redirect:/admin/manga";
    }
}
