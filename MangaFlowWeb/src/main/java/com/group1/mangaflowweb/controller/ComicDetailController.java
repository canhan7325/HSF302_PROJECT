package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import com.group1.mangaflowweb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ComicDetailController {

    private final ComicService comicService;
    private final BookmarkService bookmarkService;
    private final UserService userService;
    private final UserContextService userContextService;

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

    @GetMapping("/upload-comic")
    public String showCreateComicForm(Model model) {
        model.addAttribute("comic", new ComicRequest()); // Thêm một đối tượng trống để form binding
        return "author/upload-comic";
    }

    @PostMapping("/upload-comic")
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

        // Handle cover upload (optional)
        if (coverFile != null && !coverFile.isEmpty()) {
            try {
                String originalName = coverFile.getOriginalFilename();
                String ext = "";
                if (originalName != null) {
                    int dot = originalName.lastIndexOf('.');
                    if (dot >= 0 && dot < originalName.length() - 1) {
                        ext = originalName.substring(dot);
                    }
                }

                String safeName = UUID.randomUUID() + ext;
                Path uploadDir = Paths.get("D:/uploads");
                Files.createDirectories(uploadDir);
                Path target = uploadDir.resolve(safeName);
                Files.copy(coverFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

                comicRequest.setCoverImg(safeName);
            } catch (IOException e) {
                model.addAttribute("error", "Upload cover image failed.");
                return "author/upload-comic";
            }
        }

        comicService.create(comicRequest);
        return "redirect:/author/dashboard";
    }
}