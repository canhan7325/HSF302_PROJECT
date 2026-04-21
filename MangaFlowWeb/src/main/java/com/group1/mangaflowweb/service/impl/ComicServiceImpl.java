package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.admin.ComicAdRequest;
import com.group1.mangaflowweb.dto.response.admin.ComicAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Genres;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.util.SlugUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComicServiceImpl implements ComicService {

    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;

    public ComicServiceImpl(ComicRepository comicRepository, GenreRepository genreRepository) {
        this.comicRepository = comicRepository;
        this.genreRepository = genreRepository;
    }

    @Override
    public Page<ComicAdminResponse> getComicsPage(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return comicRepository.findByTitleContainingIgnoreCase(search, pageable)
                    .map(this::toComicAdminResponse);
        }
        return comicRepository.findAll(pageable).map(this::toComicAdminResponse);
    }

    @Override
    public ComicAdminResponse getComicById(Integer id) {
        Comics comic = comicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + id));
        return toComicAdminResponse(comic);
    }

    @Override
    @Transactional
    public void createComic(ComicAdRequest form) {
        LocalDateTime now = LocalDateTime.now();
        Comics comic = new Comics();
        comic.setTitle(form.getTitle());
        comic.setSlug(SlugUtils.toSlug(form.getTitle()));
        comic.setDescription(form.getDescription());
        comic.setCoverImg(form.getCoverImg());
        comic.setStatus(ComicEnum.ONGOING);
        comic.setViewCount(0);
        comic.setCreatedAt(now);
        comic.setUpdatedAt(now);
        comic.setGenres(resolveGenres(form.getGenreIds()));
        comicRepository.save(comic);
    }

    @Override
    @Transactional
    public void updateComic(Integer id, ComicAdRequest form) {
        Comics comic = comicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + id));
        comic.setTitle(form.getTitle());
        comic.setSlug(SlugUtils.toSlug(form.getTitle()));
        comic.setDescription(form.getDescription());
        comic.setCoverImg(form.getCoverImg());
        if (form.getStatus() != null) {
            comic.setStatus(form.getStatus());
        }
        comic.setGenres(resolveGenres(form.getGenreIds()));
        comic.setUpdatedAt(LocalDateTime.now());
        comicRepository.save(comic);
    }

    @Override
    @Transactional
    public void softDeleteComic(Integer id) {
        Comics comic = comicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + id));
        comic.setStatus(ComicEnum.CANCELED);
        comicRepository.save(comic);
    }

    @Override
    public List<GenreAdminResponse> getAllGenresWithCount() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreAdminResponse(
                        g.getGenreId(),
                        g.getName(),
                        g.getSlug(),
                        g.getComics().size()))
                .sorted(Comparator.comparingLong(GenreAdminResponse::comicCount).reversed())
                .toList();
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

    // --- helpers ---

    private ComicAdminResponse toComicAdminResponse(Comics comic) {
        String uploaderUsername = comic.getUser() != null ? comic.getUser().getUsername() : null;
        int chapterCount = comic.getChapters() != null ? comic.getChapters().size() : 0;
        return new ComicAdminResponse(
                comic.getComicId(),
                comic.getTitle(),
                comic.getSlug(),
                comic.getStatus(),
                comic.getViewCount(),
                chapterCount,
                uploaderUsername,
                comic.getCoverImg(),
                comic.getCreatedAt(),
                comic.getUpdatedAt()
        );
    }

    private List<Genres> resolveGenres(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return new ArrayList<>();
        }
        return genreRepository.findAllById(genreIds);
    }
}
