package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.ComicRequest;
import org.springframework.stereotype.Service;

@Service
public interface ComicService {
    void addComic(ComicRequest comicRequest);
}
