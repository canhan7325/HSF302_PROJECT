package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.PageRequest;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Pages;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PageServiceImpl implements PageService {
    @Autowired
    private PageRepository pageRepository;

    @Override
    public void addPage(PageRequest pageRequest, Chapters chapter) {
        Pages pages = Pages.builder()
                .pageNumber(pageRequest.getPageNumber())
                .imgPath(pageRequest.getImgPath())
                .chapter(chapter)
                .build();
        pageRepository.save(pages);
    }
}
