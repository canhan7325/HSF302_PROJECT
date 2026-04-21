package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;

import java.util.List;

public interface PageService {

    List<PageAdminResponse> getPagesByChapter(Integer chapterId);

    String uploadAndAddPage(Integer chapterId, org.springframework.web.multipart.MultipartFile file);

    void addPage(Integer chapterId, String imgPath);

    void deletePage(Integer pageId);

    void reorderPages(Integer chapterId, List<Integer> orderedPageIds);
}
