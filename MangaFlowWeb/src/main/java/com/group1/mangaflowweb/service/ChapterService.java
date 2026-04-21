package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.ChapterAdRequest;
import com.group1.mangaflowweb.dto.response.admin.ChapterAdminResponse;

import java.util.List;

public interface ChapterService {

    List<ChapterAdminResponse> getChaptersByComic(Integer comicId);

    ChapterAdminResponse getChapterById(Integer chapterId);

    void createChapter(Integer comicId, ChapterAdRequest form);

    void updateChapter(Integer chapterId, ChapterAdRequest form);

    void deleteChapter(Integer chapterId);
}
