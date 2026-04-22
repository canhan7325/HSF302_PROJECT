package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.comic.ComicSearchDTO;
import com.group1.mangaflowweb.dto.request.admin.ComicAdRequest;
import com.group1.mangaflowweb.dto.response.admin.ComicAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreAdminResponse;
import com.group1.mangaflowweb.dto.comic.ComicRequest;
import com.group1.mangaflowweb.dto.comic.ComicResponse;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.util.SlugUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ComicServiceImpl implements ComicService {

    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final ImageUrlResolver imageUrlResolver;

    public ComicServiceImpl(ComicRepository comicRepository, GenreRepository genreRepository, UserRepository userRepository, ImageUrlResolver imageUrlResolver) {
        this.comicRepository = comicRepository;
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.imageUrlResolver = imageUrlResolver;
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
                        g.getGenreComics().size()))
                .sorted(Comparator.comparingLong(GenreAdminResponse::getComicCount).reversed())
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
                        .sorted(Comparator.comparing(Comics::getViewCount).reversed())
                        .collect(Collectors.toList());
            } else if ("least-viewed".equalsIgnoreCase(filterBy)) {
                comics = comics.stream()
                        .sorted(Comparator.comparing(Comics::getViewCount))
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
    @Override
    public ComicResponse create(ComicRequest request) {
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        comicRepository.findBySlug(request.getSlug())
                .ifPresent(comic -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
                });

        Comics comic = Comics.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .description(request.getDescription())
                .coverImg(imageUrlResolver.normalizeForStorage(request.getCoverImg()))
                .status(request.getStatus())
                .user(user)
                .viewCount(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return toResponse(comicRepository.save(comic));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getById(Integer comicId) {
        return toResponse(findComicOrThrow(comicId));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicResponse getBySlug(String slug) {
        return toResponse(comicRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getAll() {
        return comicRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicResponse> getByUserId(Integer userId) {
        return comicRepository.findByUser_UserId(userId).stream().map(this::toResponse).toList();
    }

    @Override
    public ComicResponse update(Integer comicId, ComicRequest request) {
        Comics comic = findComicOrThrow(comicId);
        com.group1.mangaflowweb.entity.Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        comicRepository.findBySlug(request.getSlug())
                .filter(existing -> !existing.getComicId().equals(comicId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
                });

        comic.setTitle(request.getTitle());
        comic.setSlug(request.getSlug());
        comic.setDescription(request.getDescription());
        comic.setCoverImg(imageUrlResolver.normalizeForStorage(request.getCoverImg()));
        comic.setStatus(request.getStatus());
        comic.setUser(user);
        comic.setUpdatedAt(LocalDateTime.now());

        return toResponse(comicRepository.save(comic));
    }

    @Override
    public void delete(Integer comicId) {
        Comics comic = findComicOrThrow(comicId);
        comicRepository.delete(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicSearchDTO> searchByTitle(String query) {
        String normalizedKeyword = query == null ? "" : query.trim();
        if (normalizedKeyword.isEmpty()) {
            return List.of();
        }

        int limit = 8;
        Map<Integer, Comics> uniqueMatches = new LinkedHashMap<>();

        comicRepository.findByTitleContainingIgnoreCase(normalizedKeyword, PageRequest.of(0, limit))
                .forEach(comic -> uniqueMatches.put(comic.getComicId(), comic));

        if (uniqueMatches.size() < limit) {
            comicRepository.findBySlugContainingIgnoreCase(normalizedKeyword, PageRequest.of(0, limit))
                    .forEach(comic -> uniqueMatches.putIfAbsent(comic.getComicId(), comic));
        }

        return uniqueMatches.values().stream()
                .limit(limit)
                .map(comic -> ComicSearchDTO.builder()
                        .id(comic.getComicId())
                        .title(comic.getTitle())
                        .slug(comic.getSlug())
                        .coverImage(imageUrlResolver.resolve(comic.getCoverImg()))
                        .build())
                .toList();
    }

    private Comics findComicOrThrow(Integer comicId) {
        return comicRepository.findById(comicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));
    }

    private ComicResponse toResponse(Comics comic) {
        return ComicResponse.builder()
                .comicId(comic.getComicId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .description(comic.getDescription())
                .coverImg(comic.getCoverImg())
                .status(comic.getStatus())
                .viewCount(comic.getViewCount())
                .followerCount(comic.getBookmarks() != null ? comic.getBookmarks().size() : 0)
                .bookmarked(false)  // Default to false - will be set by controller
                .userId(comic.getUser() != null ? comic.getUser().getUserId() : null)
                .authorName(comic.getUser() != null ? comic.getUser().getUsername() : null)
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .chapters(comic.getChapters() != null ? comic.getChapters().stream()
                                                        .sorted(Comparator.comparing(chapter -> chapter.getChapterNumber() == null ? 0 : chapter.getChapterNumber()))
                                                        .map(chapter -> ComicResponse.ChapterSummary.builder()
                                                                        .chapterId(chapter.getChapterId())
                                                                        .chapterNumber(chapter.getChapterNumber())
                                                                        .title(chapter.getTitle())
                                                                        .createdAt(chapter.getCreatedAt())
                                                                        .build())
                                                        .toList() : new ArrayList<>())
                .genres(comic.getGenreComics() != null ? comic.getGenreComics().stream()
                                                         .filter(gc -> gc.getGenre() != null)
                                                         .map(gc -> ComicResponse.GenreSummary.builder()
                                                                    .genreId(gc.getGenre().getGenreId())
                                                                    .name(gc.getGenre().getName())
                                                                    .build())
                                                         .toList() : new ArrayList<>())
                .build();
    }
}
