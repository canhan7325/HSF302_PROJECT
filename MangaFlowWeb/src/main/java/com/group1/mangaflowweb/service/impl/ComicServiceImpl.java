package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.dto.comic.ComicResponse;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.ComicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ComicServiceImpl implements ComicService {
    private final ComicRepository comicRepository;
    private final UserRepository userRepository;

    @Override
    public ComicResponse create(ComicRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        comicRepository.findBySlug(request.getSlug())
                .ifPresent(comic -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
                });

        Comics comic = Comics.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .coverImg(request.getCoverImg())
                .status(request.getStatus())
                .user(user)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toResponse(comicRepository.save(comic));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getById(Integer comicId) {
        return toResponse(findComicOrThrow(comicId));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getBySlug(String slug) {
        return toResponse(comicRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getAll() {
        return comicRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getByUserId(Integer userId) {
        return comicRepository.findByUser_UserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public ComicResponse update(Integer comicId, ComicRequest request) {
        Comics comic = findComicOrThrow(comicId);
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        comicRepository.findBySlug(request.getSlug())
                .filter(existing -> !existing.getComicId().equals(comicId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
                });

        comic.setTitle(request.getTitle());
        comic.setSlug(request.getSlug());
        comic.setDescription(request.getDescription());
        comic.setCoverImg(request.getCoverImg());
        comic.setStatus(request.getStatus());
        comic.setUser(user);
        comic.setUpdatedAt(LocalDateTime.now());

        return toResponse(comicRepository.save(comic));
    }

    @Override
    public void delete(Integer comicId) {
        Comics comic = findComicOrThrow(comicId);
        comicRepository.delete(comic);
    }

    private Comics findComicOrThrow(Integer comicId) {
        return comicRepository.findById(comicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));
    }

    private ComicResponse toResponse(Comics comic) {
        return ComicResponse.builder()
                .comicId(comic.getComicId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .description(comic.getDescription())
                .coverImg(comic.getCoverImg())
                .status(comic.getStatus())
                .viewCount(comic.getViewCount())
                .followerCount(comic.getBookmarks() != null ? comic.getBookmarks().size() : 0)
                .bookmarked(false)  // Default to false - will be set by controller
                .userId(comic.getUser() != null ? comic.getUser().getUserId() : null)
                .authorName(comic.getUser() != null ? comic.getUser().getUsername() : null)
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .chapters(comic.getChapters() != null ? comic.getChapters().stream()
                        .map(chapter -> ComicResponse.ChapterSummary.builder()
                                .chapterId(chapter.getChapterId())
                                .chapterNumber(chapter.getChapterNumber())
                                .title(chapter.getTitle())
                                .createdAt(chapter.getCreatedAt())
                                .build())
                        .toList() : new ArrayList<>())
                .genres(new ArrayList<>())  // Empty list for now - genres relationship may not be loaded
                .build();
    }
}
