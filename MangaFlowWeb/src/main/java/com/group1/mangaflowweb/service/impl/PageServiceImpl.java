package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Pages;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.CloudinaryUploadService;
import com.group1.mangaflowweb.service.PageService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;
@Service
public class PageServiceImpl implements PageService {
    private final ImageUrlResolver imageUrlResolver;
    private final PageRepository pageRepository;
    private final ChapterRepository chapterRepository;
    private final CloudinaryUploadService cloudinaryUploadService;

    public PageServiceImpl(PageRepository pageRepository,
                           ChapterRepository chapterRepository,
                           ImageUrlResolver imageUrlResolver,
                           CloudinaryUploadService cloudinaryUploadService) {
        this.pageRepository = pageRepository;
        this.chapterRepository = chapterRepository;
        this.imageUrlResolver = imageUrlResolver;
        this.cloudinaryUploadService = cloudinaryUploadService;
    }

    @Override
    public List<PageAdminResponse> getPagesByChapter(Integer chapterId) {
        return pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(p -> new PageAdminResponse(p.getPageId(), p.getPageNumber(), imageUrlResolver.resolve(p.getImgPath())))
                .toList();
    }

    @Override
    @Transactional
    public String uploadAndAddPage(Integer chapterId, MultipartFile file) {
        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found: " + chapterId));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Empty page file");
        }

        int nextNum = pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId).size() + 1;
        String publicId = "chapters/" + chapterId + "/page_" + String.format("%04d", nextNum);

        String storedId;
        try {
            storedId = cloudinaryUploadService.uploadImage(file, publicId);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload page image to Cloudinary: " + e.getMessage(), e);
        }

        Pages page = new Pages();
        page.setChapter(chapter);
        page.setPageNumber(nextNum);

        page.setImgPath(imageUrlResolver.normalizeForStorage(storedId));
        pageRepository.save(page);

        return page.getImgPath();
    }

    @Override
    @Transactional
    public void addPage(Integer chapterId, String imgPath) {
        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found: " + chapterId));
        int nextNum = pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId).size() + 1;

        Pages page = new Pages();
        page.setChapter(chapter);
        page.setPageNumber(nextNum);
        page.setImgPath(imageUrlResolver.normalizeForStorage(imgPath));
        pageRepository.save(page);
    }

    @Override
    @Transactional
    public void deletePage(Integer pageId) {
        pageRepository.deleteById(pageId);
    }

    @Override
    @Transactional
    public void reorderPages(Integer chapterId, List<Integer> orderedPageIds) {
        for (int i = 0; i < orderedPageIds.size(); i++) {
            final int pageNum = i + 1;
            pageRepository.findById(orderedPageIds.get(i)).ifPresent(p -> {
                p.setPageNumber(pageNum);
                pageRepository.save(p);
            });
        }
    }

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
                .imgPath(imageUrlResolver.normalizeForStorage(request.getImgPath()))
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
    @Transactional(readOnly = true)
    public Optional<String> getFirstPageImagePath(Integer chapterId) {
        if (chapterId == null) {
            return Optional.empty();
        }

        return pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(Pages::getImgPath)
                .filter(path -> path != null && !path.isBlank())
                .findFirst();
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
        page.setImgPath(imageUrlResolver.normalizeForStorage(request.getImgPath()));
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
                .imgPath(imageUrlResolver.resolve(page.getImgPath()))
                .build();
    }
}

