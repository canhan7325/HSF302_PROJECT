package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Pages;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.PageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PageServiceImpl implements PageService {
    private final PageRepository pageRepository;
    private final ChapterRepository chapterRepository;

    @Override
    public PageResponse create(PageRequest request) {
        Chapters chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));

        pageRepository.findByChapter_ChapterIdAndPageNumber(request.getChapterId(), request.getPageNumber())
                .ifPresent(page -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Page number already exists for this chapter");
                });

        Pages page = Pages.builder()
                .chapter(chapter)
                .pageNumber(request.getPageNumber())
                .imgPath(request.getImgPath())
                .build();

        return toResponse(pageRepository.save(page));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse getById(Integer pageId) {
        return toResponse(findPageOrThrow(pageId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageResponse> getAll() {
        return pageRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageResponse> getByChapterId(Integer chapterId) {
        return pageRepository.findByChapter_ChapterId(chapterId).stream().map(this::toResponse).toList();
    }

    @Override
    public PageResponse update(Integer pageId, PageRequest request) {
        Pages page = findPageOrThrow(pageId);
        Chapters chapter = chapterRepository.findById(request.getChapterId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));

        pageRepository.findByChapter_ChapterIdAndPageNumber(request.getChapterId(), request.getPageNumber())
                .filter(existing -> !existing.getPageId().equals(pageId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Page number already exists for this chapter");
                });

        page.setChapter(chapter);
        page.setPageNumber(request.getPageNumber());
        page.setImgPath(request.getImgPath());
        return toResponse(pageRepository.save(page));
    }

    @Override
    public void delete(Integer pageId) {
        Pages page = findPageOrThrow(pageId);
        pageRepository.delete(page);
    }

    private Pages findPageOrThrow(Integer pageId) {
        return pageRepository.findById(pageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));
    }

    private PageResponse toResponse(Pages page) {
        return PageResponse.builder()
                .pageId(page.getPageId())
                .chapterId(page.getChapter() != null ? page.getChapter().getChapterId() : null)
                .pageNumber(page.getPageNumber())
                .imgPath(page.getImgPath())
                .build();
    }
}
