package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.UserService;
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
    private final UserService userService;

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
}





