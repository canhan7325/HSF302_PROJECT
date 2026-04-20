package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.PageRequest;
import com.group1.mangaflowweb.entity.Chapters;
import org.springframework.stereotype.Service;

@Service
public interface PageService {
    void addPage(PageRequest pageRequest, Chapters chapter);
}
