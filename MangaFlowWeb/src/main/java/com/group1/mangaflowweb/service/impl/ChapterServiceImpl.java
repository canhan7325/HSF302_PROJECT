package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.dto.request.PageRequest;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChapterServiceImpl implements ChapterService {
    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private PageService pageService;

    @Override
    public void addChapter(ChapterRequest chapterRequest, Comics comic) {
        Chapters chapter = Chapters.builder()
                .chapterNumber(chapterRequest.getChapterNumber())
                .title(chapterRequest.getTitle())
                .comic(comic)
                .build();

        chapter = chapterRepository.save(chapter);

        if (chapterRequest.getPageRequests() != null) {
            for (PageRequest pageRequest : chapterRequest.getPageRequests()) {
                pageService.addPage(pageRequest, chapter);
            }
        }
    }
}
