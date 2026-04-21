package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.UserService;
import com.group1.mangaflowweb.repository.UserRepository;
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
    private final UserRepository userRepository;

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

    @GetMapping("/comic-create")
    public String showCreateComicForm(Model model) {
        model.addAttribute("comic", new ComicRequest()); // Thêm một đối tượng trống để form binding
        return "comic/create";
    }

    @PostMapping("/comic-create")
    public String createComic(@Valid @ModelAttribute("comic") ComicRequest comicRequest,
                              BindingResult bindingResult,
                              @RequestParam(required = false) String username,
                              @RequestParam(required = false, name = "coverFile") MultipartFile coverFile,
                              Model model) {

        // IMPORTANT: userId is @NotNull. If UI provides username, resolve userId BEFORE validation check.
        if (comicRequest.getUserId() == null) {
            if (username != null && !username.isBlank()) {
                Integer userId = userRepository.findByUsername(username.trim())
                        .map(com.group1.mangaflowweb.entity.Users::getUserId)
                        .orElse(null);
                if (userId != null) {
                    comicRequest.setUserId(userId);
                }
            }
        }

        if (bindingResult.hasErrors()) {
            // Provide a more useful error message than the generic one.
            String details = bindingResult.getFieldErrors().stream()
                    .map(err -> err.getField() + ": " + err.getDefaultMessage())
                    .distinct()
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("Please fill all required fields correctly.");
            model.addAttribute("error", details);
            return "comic/create";
        }

        // If userId is still null, username was missing/invalid.
        if (comicRequest.getUserId() == null) {
            model.addAttribute("error", "Username is required (must be an existing account).");
            return "comic/create";
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
                return "comic/create";
            }
        }

        comicService.create(comicRequest);
        return "redirect:/";
    }
}
