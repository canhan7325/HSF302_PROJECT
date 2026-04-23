package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
public class ComicDetailController {
    private final GenreService genreService;
    private final ComicService comicService;
    private final BookmarkService bookmarkService;
    private final ReadingHistoryService readingHistoryService;
    private final UserService userService;
    private final UserContextService userContextService;
    private final CloudinaryUploadService cloudinaryUploadService;

    @GetMapping("/comic/{slug}")
    public String getComicDetail(@PathVariable String slug, Model model) {
        // Lấy comic từ service (trả về DTO)
        var comic = comicService.getBySlug(slug);

        // Kiểm tra xem user hiện tại có bookmark comic này không
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
            String username = authentication.getName();
            try {
                var user = userService.findByUsername(username);
                if (user != null) {
                    boolean isBookmarked = bookmarkService.isBookmarked(user.getUserId(), comic.getComicId());
                    comic.setBookmarked(isBookmarked);
                } else {
                    comic.setBookmarked(false);
                }
            } catch (Exception e) {
                comic.setBookmarked(false);
            }
        } else {
            comic.setBookmarked(false);
        }

        model.addAttribute("comic", comic);
        model.addAttribute("hasChapters", comic.getChapters() != null && !comic.getChapters().isEmpty());
        return "comic-detail";
    }

    @GetMapping("/author/upload-comic")
    public String showCreateComicForm(Model model) {
        model.addAttribute("comic", new ComicRequest()); // Thêm một đối tượng trống để form binding
        model.addAttribute("genres", genreService.getAllGenres());
        return "author/upload-comic";
    }

    @GetMapping("/comic/{comicId}/read-now")
    public String readNow(@PathVariable Integer comicId) {
        Integer currentUserId = userContextService.getCurrentUser()
                .map(com.group1.mangaflowweb.entity.Users::getUserId)
                .orElse(null);

        Integer chapterId = readingHistoryService.resolveReadNowChapterId(currentUserId, comicId);
        if (chapterId == null) {
            return "redirect:/";
        }
        return "redirect:/chapters/" + chapterId + "/read";
    }

    @PostMapping("/author/upload-comic")
    public String createComic(@Valid @ModelAttribute("comic") ComicRequest comicRequest,
                              BindingResult bindingResult,
                              @RequestParam(required = false, name = "coverFile") MultipartFile coverFile,
                              Model model) {

        // Resolve userId from current logged-in user (server-side).
        if (comicRequest.getUserId() == null) {
            Integer currentUserId = userContextService.getCurrentUser()
                    .map(com.group1.mangaflowweb.entity.Users::getUserId)
                    .orElse(null);

            // Fallback: if session doesn't have userId, resolve from Spring Security username.
            if (currentUserId == null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                    try {
                        var user = userService.findByUsername(auth.getName());
                        if (user != null) currentUserId = user.getUserId();
                    } catch (Exception ignored) {
                        // keep null
                    }
                }
            }

            if (currentUserId != null) {
                comicRequest.setUserId(currentUserId);
            }
        }

        // Validate request fields
        if (bindingResult.hasErrors()) {
            model.addAttribute("genres", genreService.getAllGenres());
            String details = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .distinct()
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Please fill all required fields correctly.");
            model.addAttribute("error", details);
            return "author/upload-comic";
        }

        if (comicRequest.getUserId() == null) {
            model.addAttribute("error", "You must be logged in to create a comic.");
            return "author/upload-comic";
        }

        // Upload cover to Cloudinary (optional). Save public_id into coverImg.
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String slug = safeSlug(comicRequest.getSlug() != null ? comicRequest.getSlug() : comicRequest.getTitle());
                if (slug.isBlank()) slug = "comic";

                String publicId = "comics/" + slug + "/cover";
                String storedId = cloudinaryUploadService.uploadImage(coverFile, publicId);
                comicRequest.setCoverImg(storedId);
            } catch (IOException e) {
                model.addAttribute("error", "Upload cover image failed: " + e.getMessage());
                return "author/upload-comic";
            }
        }

        comicService.create(comicRequest);
        return "redirect:/author/dashboard";
    }

    private String safeSlug(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return "";

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('đ', 'd');
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        return s;
    }
}