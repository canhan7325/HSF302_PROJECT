package com.group1.mangaflowweb.controller;

import com.group1.mangaflowweb.entity.ReadingHistories;
import com.group1.mangaflowweb.service.ReadingHistoryService;
import com.group1.mangaflowweb.service.PageService;
import com.group1.mangaflowweb.service.UserContextService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Controller
@RequestMapping("/reading-histories")
@RequiredArgsConstructor
public class ReadingHistoryController {

    private final ReadingHistoryService readingHistoryService;
    private final PageService pageService;
    private final UserContextService userContextService;
    private final ImageUrlResolver imageUrlResolver;

    @GetMapping
    public String readingHistories(Model model) {
        Integer currentUserId = userContextService.getCurrentUser().map(com.group1.mangaflowweb.entity.Users::getUserId).orElse(null);
        model.addAttribute("currentUserId", currentUserId);

        if (currentUserId == null) {
            model.addAttribute("histories", List.of());
            return "reading-histories";
        }

        List<ReadingHistories> histories = readingHistoryService.findByUserIdOrderByReadAtDesc(currentUserId);
        List<ReadingHistoryItemView> views = histories.stream()
                .filter(Objects::nonNull)
                .filter(h -> h.getChapter() != null && h.getChapter().getComic() != null)
                .map(this::toView)
                .toList();

        model.addAttribute("histories", views);
        return "reading-histories";
    }

    private ReadingHistoryItemView toView(ReadingHistories rh) {
        var chapter = rh.getChapter();
        var comic = chapter.getComic();

        Integer chapterNumber = chapter.getChapterNumber();
        String chapterText;
        if (chapterNumber != null && chapter.getTitle() != null && !chapter.getTitle().isBlank()) {
            chapterText = "Chapter " + chapterNumber + ": " + chapter.getTitle();
        } else if (chapterNumber != null) {
            chapterText = "Chapter " + chapterNumber;
        } else {
            chapterText = "Chapter";
        }

        LocalDateTime readAt = rh.getReadAt();
        String firstPageImage = pageService.getFirstPageImagePath(chapter.getChapterId())
                .map(imageUrlResolver::resolve)
                .orElse("");
        String fallbackCover = imageUrlResolver.resolve(comic.getCoverImg());
        String thumbnail = !firstPageImage.isBlank() ? firstPageImage : (!fallbackCover.isBlank() ? fallbackCover : null);

        return ReadingHistoryItemView.builder()
                .comicId(comic.getComicId())
                .comicSlug(comic.getSlug())
                .comicTitle(comic.getTitle())
                .coverUrl(thumbnail)
                .chapterId(chapter.getChapterId())
                .chapterText(chapterText)
                .continueUrl(chapter.getChapterId() != null ? ("/chapters/" + chapter.getChapterId() + "/read") : null)
                .readAt(readAt)
                .timeAgo(formatTimeAgo(readAt))
                .timestampText(formatTimestamp(readAt))
                .genreName(null) // optional, can be filled when you want join genres
                .build();
    }

    private String formatTimeAgo(LocalDateTime readAt) {
        if (readAt == null) return "";
        long minutes = ChronoUnit.MINUTES.between(readAt, LocalDateTime.now());
        if (minutes < 1) return "JUST NOW";
        if (minutes < 60) return minutes + " MINUTES AGO";
        long hours = ChronoUnit.HOURS.between(readAt, LocalDateTime.now());
        if (hours < 24) return hours + " HOURS AGO";
        long days = ChronoUnit.DAYS.between(readAt, LocalDateTime.now());
        return days + " DAYS AGO";
    }

    private String formatTimestamp(LocalDateTime readAt) {
        if (readAt == null) return "";
        LocalDate date = readAt.toLocalDate();
        LocalDate today = LocalDate.now();
        if (date.equals(today.minusDays(1))) {
            return "YESTERDAY, " + readAt.format(DateTimeFormatter.ofPattern("h:mma")).toUpperCase();
        }
        long days = ChronoUnit.DAYS.between(date, today);
        if (days >= 2) {
            return days + " DAYS AGO";
        }
        return readAt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    @Data
    @Builder
    private static class ReadingHistoryItemView {
        private Integer comicId;
        private String comicSlug;
        private String comicTitle;
        private String coverUrl;

        private Integer chapterId;
        private String chapterText;

        private String genreName;

        private String continueUrl;

        private LocalDateTime readAt;
        private String timeAgo;
        private String timestampText;
    }
}
