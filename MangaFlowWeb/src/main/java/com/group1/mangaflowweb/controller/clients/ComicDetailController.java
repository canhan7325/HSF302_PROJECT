package com.group1.mangaflowweb.controller.clients;

import com.group1.mangaflowweb.dto.user.UserDTO;
import com.group1.mangaflowweb.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ComicDetailController {
    private final ComicService comicService;
    private final BookmarkService bookmarkService;
    private final ReadingHistoryService readingHistoryService;
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
                UserDTO user = userService.findByUsername(username);
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
        return "clients/comics/comic-detail";
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

}




