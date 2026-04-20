package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.service.ComicService;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComicServiceImpl implements ComicService {
    
    private final ComicRepository comicRepository;
    
    public ComicServiceImpl(ComicRepository comicRepository) {
        this.comicRepository = comicRepository;
    }
    
    @Override
    public long getTotalComics() {
        return comicRepository.count();
    }
    
    @Override
    public List<Comics> getAllComics() {
        return comicRepository.findAll();
    }
    
    @Override
    public List<Comics> searchComicsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllComics();
        }
        return comicRepository.findByTitleContainingIgnoreCase(name.trim());
    }
    
    @Override
    public List<Comics> getComicsWithSort(String sortBy, String sortOrder) {
        List<Comics> comics = getAllComics();
        
        if (sortBy == null) sortBy = "id";
        if (sortOrder == null) sortOrder = "asc";
        
        final String order = sortOrder;
        final String field = sortBy;
        
        if ("name".equalsIgnoreCase(field)) {
            comics.sort((a, b) -> "asc".equalsIgnoreCase(order) 
                ? a.getTitle().compareTo(b.getTitle())
                : b.getTitle().compareTo(a.getTitle()));
        } else if ("views".equalsIgnoreCase(field)) {
            comics.sort((a, b) -> "asc".equalsIgnoreCase(order)
                ? a.getViewCount().compareTo(b.getViewCount())
                : b.getViewCount().compareTo(a.getViewCount()));
        } else {
            comics.sort((a, b) -> "asc".equalsIgnoreCase(order)
                ? a.getComicId().compareTo(b.getComicId())
                : b.getComicId().compareTo(a.getComicId()));
        }
        
        return comics;
    }
    
    @Override
    public List<Comics> getComicsWithFilter(String sortBy, String sortOrder, String filterBy) {
        List<Comics> comics = getComicsWithSort(sortBy, sortOrder);
        
        if (filterBy != null) {
            if ("most-viewed".equalsIgnoreCase(filterBy)) {
                comics = comics.stream()
                        .sorted((a, b) -> b.getViewCount().compareTo(a.getViewCount()))
                        .collect(Collectors.toList());
            } else if ("least-viewed".equalsIgnoreCase(filterBy)) {
                comics = comics.stream()
                        .sorted((a, b) -> a.getViewCount().compareTo(b.getViewCount()))
                        .collect(Collectors.toList());
            }
        }
        
        return comics;
    }
}

