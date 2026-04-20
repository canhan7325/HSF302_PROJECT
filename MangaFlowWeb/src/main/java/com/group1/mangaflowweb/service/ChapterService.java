package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.dto.response.ChapterAdminResponse;

import java.util.List;

public interface ChapterService {

    List<ChapterAdminResponse> getChaptersByComic(Integer comicId);

    ChapterAdminResponse getChapterById(Integer chapterId);

    void createChapter(Integer comicId, ChapterRequest form);

    void updateChapter(Integer chapterId, ChapterRequest form);

    void deleteChapter(Integer chapterId);
}
