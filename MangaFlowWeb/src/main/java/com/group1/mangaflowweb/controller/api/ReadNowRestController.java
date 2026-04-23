package com.group1.mangaflowweb.controller.api;

import com.group1.mangaflowweb.repository.ReadingHistoryRepository;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.service.UserContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/comic")
@RequiredArgsConstructor
public class ReadNowRestController {

    private final ComicService comicService;
    private final UserContextService userContextService;
    private final ReadingHistoryRepository readingHistoryRepository;

    @GetMapping("/{comicId}/read-now")
    public ResponseEntity<Map<String, String>> readNow(@PathVariable Integer comicId) {
        try {
            // Get current user
            Integer currentUserId = userContextService.getCurrentUser()
                    .map(com.group1.mangaflowweb.entity.Users::getUserId)
                    .orElse(null);

            Integer chapterId = null;

            if (currentUserId == null) {
                // Not logged in, redirect to chapter 1
                var comic = comicService.getById(comicId);
                if (comic.getChapters() != null && !comic.getChapters().isEmpty()) {
                    chapterId = comic.getChapters().get(0).getChapterId();
                }
            } else {
                // User is logged in - check reading history
                var readingHistory = readingHistoryRepository
                        .findFirstByUser_UserIdAndChapter_Comic_ComicIdOrderByReadAtDesc(currentUserId, comicId);

                if (readingHistory.isPresent()) {
                    // User has reading history - use the last chapter read
                    chapterId = readingHistory.get().getChapter().getChapterId();
                } else {
                    // No reading history - use first chapter
                    var comic = comicService.getById(comicId);
                    if (comic.getChapters() != null && !comic.getChapters().isEmpty()) {
                        chapterId = comic.getChapters().get(0).getChapterId();
                    }
                }
            }

            Map<String, String> response = new HashMap<>();
            response.put("redirect", chapterId != null ? "/chapters/" + chapterId + "/read" : "/");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
