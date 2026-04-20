package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.entity.Comics;
import java.util.List;

public interface ComicService {
    long getTotalComics();
    List<Comics> getAllComics();
    List<Comics> searchComicsByName(String name);
    List<Comics> getComicsWithSort(String sortBy, String sortOrder);
    List<Comics> getComicsWithFilter(String sortBy, String sortOrder, String filterBy);
}


