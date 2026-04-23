package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.comic.ComicDTO;
import com.group1.mangaflowweb.dto.user.UserDTO;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;

@Controller
@RequestMapping("/author")
@RequiredArgsConstructor
public class AuthorDashboardController {
    private final UserContextService userContextService;
    private final ComicService comicService;
    private final ChapterService chapterService;
    private final PageService pageService;
    private final GenreService genreService;
    private final CloudinaryUploadService cloudinaryUploadService;
    private final UserService userService;

    @GetMapping({"/dashboard", ""})
    public String dashboard(Model model) {
        Integer userId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        model.addAttribute("currentUserId", userId);

        if (userId == null) {
            model.addAttribute("error", "Hãy đăng nhập để đăng truyện.");
            model.addAttribute("myComics", java.util.List.of());
            return "clients/author/dashboard";
        }

        model.addAttribute("myComics", comicService.getByUserId(userId));
        return "clients/author/dashboard";
    }

    @GetMapping("/upload-comic")
    public String showCreateComicForm(Model model) {
        model.addAttribute("comic", new ComicDTO()); // Thêm một đối tượng trống để form binding
        model.addAttribute("genres", genreService.getAllGenres());
        return "clients/author/upload-comic";
    }

    @PostMapping("/upload-comic")
    public String createComic(@Valid @ModelAttribute("comic") ComicDTO comicRequest,
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
                        UserDTO user = userService.findByUsername(auth.getName());
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
                    .orElse("Hãy điền các hết các ô bắt buộc.");
            model.addAttribute("error", details);
            return "clients/author/upload-comic";
        }

        if (comicRequest.getUserId() == null) {
            model.addAttribute("error", "Hãy đăng nhập để đăng truyện.");
            return "clients/author/upload-comic";
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
                model.addAttribute("error", "Đăng ảnh bìa thất bại: " + e.getMessage());
                return "clients/author/upload-comic";
            }
        }

        comicService.create(comicRequest);
        return "redirect:/author/dashboard";
    }

    @GetMapping("/upload-chapter")
    public String uploadChapterForm(Model model) {
        Integer userId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        model.addAttribute("currentUserId", userId);
        if (userId == null) {
            model.addAttribute("error", "Hãy đăng nhập để đăng truyện.");
            model.addAttribute("myComics", java.util.List.of());
            return "clients/author/upload-chapter";
        }

        model.addAttribute("myComics", comicService.getByUserId(userId));
        return "clients/author/upload-chapter";
    }

    @PostMapping("/upload-chapter")
    public String handleUploadChapter(@RequestParam Integer comicId,
                                      @RequestParam Integer chapterNumber,
                                      @RequestParam(required = false, name = "chapterTitle") String chapterTitle,
                                      @RequestParam(name = "pages") MultipartFile[] pages,
                                      RedirectAttributes redirectAttributes) {
        Integer currentUserId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        if (currentUserId == null) {
            redirectAttributes.addFlashAttribute("error", "Hãy đăng nhập để đăng chương.");
            return "redirect:/author/upload-chapter";
        }

        // Authorization: ensure selected comic belongs to current user
        ComicDTO comic = comicService.getById(comicId);
        if (comic.getUserId() == null || !comic.getUserId().equals(currentUserId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền đăng chương.");
            return "redirect:/author/upload-chapter";
        }

        if (pages == null || pages.length == 0) {
            redirectAttributes.addFlashAttribute("error", "Hãy chọn ít nhất 1 trang.");
            return "redirect:/author/upload-chapter";
        }

        ChapterDTO req = ChapterDTO.builder()
                .comicId(comicId)
                .chapterNumber(chapterNumber)
                .title((chapterTitle != null && !chapterTitle.isBlank()) ? chapterTitle.trim() : null)
                .build();

        Integer chapterId;
        try {
            chapterId = chapterService.create(req).getChapterId();
        } catch (ResponseStatusException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getReason());
            return "redirect:/author/upload-chapter";
        }

        int savedCount = 0;
        for (MultipartFile file : pages) {
            if (file == null || file.isEmpty()) continue;
            try {
                pageService.uploadAndAddPage(chapterId, file);
                savedCount++;
            } catch (Exception ex) {
                redirectAttributes.addFlashAttribute("error",
                        "Đăng thành công nhưng một vài trang chưa được lưu. Lỗi gần nhất: " + ex.getMessage());
                return "redirect:/chapters/" + chapterId + "/read";
            }
        }

        redirectAttributes.addFlashAttribute("message", "Chương đã đăng thành công (" + savedCount + " trang)." );
        return "redirect:/chapters/" + chapterId + "/read";
    }


    @GetMapping("/analytics")
    public String analytics() {
        return "clients/author/dashboard";
    }

    @GetMapping("/settings")
    public String settings() {
        return "clients/author/dashboard";
    }

    private String safeSlug(String input) {
        if (input == null) return "";
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.isEmpty()) return "";

        s = Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        s = s.replace('d', 'd');
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        return s;
    }
}


