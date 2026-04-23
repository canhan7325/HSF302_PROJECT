package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.page.PageDTO;
import com.group1.mangaflowweb.dto.page.PageAdminDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface PageService {

    List<PageAdminDTO> getPagesByChapter(Integer chapterId);

    String uploadAndAddPage(Integer chapterId, MultipartFile file);

    void addPage(Integer chapterId, String imgPath);

    void deletePage(Integer pageId);

    void reorderPages(Integer chapterId, List<Integer> orderedPageIds);

    PageDTO create(PageDTO request);

    PageDTO getById(Integer pageId);

    List<PageDTO> getAll();

    List<PageDTO> getByChapterId(Integer chapterId);

    Optional<String> getFirstPageImagePath(Integer chapterId);

    PageDTO update(Integer pageId, PageDTO request);

    void delete(Integer pageId);
}

