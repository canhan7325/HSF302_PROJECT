package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Pages;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.PageService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import com.group1.mangaflowweb.dto.page.PageRequest;
import com.group1.mangaflowweb.dto.page.PageResponse;
@Service
public class PageServiceImpl implements PageService {

    private final PageRepository pageRepository;
    private final ChapterRepository chapterRepository;

    public PageServiceImpl(PageRepository pageRepository, ChapterRepository chapterRepository) {
        this.pageRepository = pageRepository;
        this.chapterRepository = chapterRepository;
    }

    @Override
    public List<PageAdminResponse> getPagesByChapter(Integer chapterId) {
        return pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(p -> new PageAdminResponse(p.getPageId(), p.getPageNumber(), p.getImgPath()))
                .toList();
    }

    @Override
    @Transactional
    public String uploadAndAddPage(Integer chapterId, MultipartFile file) {
        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found: " + chapterId));

        // Build unique filename
        String original = file.getOriginalFilename();
        String ext = (original != null && original.contains("."))
                ? original.substring(original.lastIndexOf('.'))
                : ".jpg";
        String filename = "ch" + chapterId + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        // Save to disk — write to both src (permanent) and target/classes (served immediately by devtools)
        try {
            Path srcDir = Paths.get("src/main/resources/static/images/pages");
            Files.createDirectories(srcDir);
            Files.copy(file.getInputStream(), srcDir.resolve(filename));

            // Also copy to target so it's served without restart
            Path targetDir = Paths.get("target/classes/static/images/pages");
            if (Files.exists(Paths.get("target/classes"))) {
                Files.createDirectories(targetDir);
                Files.copy(srcDir.resolve(filename), targetDir.resolve(filename),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save page image: " + e.getMessage(), e);
        }

        // Persist page record
        String imgPath = "/images/pages/" + filename;
        int nextNum = (chapter.getPages() != null ? chapter.getPages().size() : 0) + 1;
        Pages page = new Pages();
        page.setChapter(chapter);
        page.setPageNumber(nextNum);
        page.setImgPath(imgPath);
        pageRepository.save(page);

        return imgPath;
    }

    @Override
    @Transactional
    public void addPage(Integer chapterId, String imgPath) {
        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found: " + chapterId));
        int nextNum = (chapter.getPages() != null ? chapter.getPages().size() : 0) + 1;
        Pages page = new Pages();
        page.setChapter(chapter);
        page.setPageNumber(nextNum);
        page.setImgPath(imgPath);
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
            pageRepository.findById(orderedPageIds.get(i)).ifPresent(p -> {
                p.setPageNumber(orderedPageIds.indexOf(p.getPageId()) + 1);
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


