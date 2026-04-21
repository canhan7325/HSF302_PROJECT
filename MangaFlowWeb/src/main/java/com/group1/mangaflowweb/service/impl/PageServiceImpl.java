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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

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
}
