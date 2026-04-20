package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.dto.request.ComicRequest;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.service.ComicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ComicServiceImpl implements ComicService {
    @Autowired
    private ComicRepository comicRepository;

    @Autowired
    private ChapterService chapterService;

    @Override
    public void addComic(ComicRequest comicRequest) {
        Comics comic = Comics.builder()
                .title(comicRequest.getTitle())
                .slug(comicRequest.getSlug())
                .description(comicRequest.getDescription())
                .coverImg(comicRequest.getCoverImg())
                .user(comicRequest.getUser())
                .build();

        comic = comicRepository.save(comic);

        if (comicRequest.getChapterRequests() != null) {
            for (ChapterRequest chapterRequest : comicRequest.getChapterRequests()) {
                chapterService.addChapter(chapterRequest, comic);
            }
        }
    }
}
