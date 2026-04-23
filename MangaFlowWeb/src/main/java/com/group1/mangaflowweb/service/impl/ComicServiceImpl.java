package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.comic.ComicAdminDTO;
import com.group1.mangaflowweb.dto.comic.ComicDTO;
import com.group1.mangaflowweb.dto.comic.ComicSummaryDTO;
import com.group1.mangaflowweb.dto.genre.GenreAdminDTO;
import com.group1.mangaflowweb.entity.*;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.repository.UsersRepository;
import com.group1.mangaflowweb.service.ComicService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import com.group1.mangaflowweb.util.SlugUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ComicServiceImpl implements ComicService {

    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final UsersRepository usersRepository;
    private final ImageUrlResolver imageUrlResolver;

    public ComicServiceImpl(ComicRepository comicRepository,
                            GenreRepository genreRepository,
                            UsersRepository usersRepository,
                            ImageUrlResolver imageUrlResolver) {
        this.comicRepository = comicRepository;
        this.genreRepository = genreRepository;
        this.usersRepository = usersRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @Override
    public Page<ComicAdminDTO> getComicsPage(Pageable pageable, String search) {
        if (search != null && !search.isBlank()) {
            return comicRepository.findByTitleContainingIgnoreCase(search, pageable)
                    .map(this::toComicAdminDTO);
        }
        return comicRepository.findAll(pageable).map(this::toComicAdminDTO);
    }

    @Override
    public ComicAdminDTO getComicById(Integer id) {
        Comics comic = comicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + id));
        return toComicAdminDTO(comic);
    }

    @Override
    @Transactional
    public void createComic(ComicAdminDTO form) {
        // Check for duplicate title
        comicRepository.findByTitleIgnoreCase(form.getTitle())
                .ifPresent(comic -> {
                    throw new IllegalArgumentException("Comic with title '" + form.getTitle() + "' already exists");
                });
        
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
        
        // Set user from uploaderId
        if (form.getUploaderId() != null) {
            Users user = usersRepository.findById(form.getUploaderId())
                    .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + form.getUploaderId()));
            comic.setUser(user);
        }
        
        Comics saved = comicRepository.save(comic);

        if (form.getGenreIds() != null && !form.getGenreIds().isEmpty()) {
            for (Integer genreId : form.getGenreIds()) {
                Genres genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("Genre not found: " + genreId));
                GenreComics gc = GenreComics.builder()
                        .id(new GenreComicsId(saved.getComicId(), genreId))
                        .comic(saved)
                        .genre(genre)
                        .build();
                saved.getGenreComics().add(gc);
            }
            comicRepository.save(saved);
        }
    }

    @Override
    @Transactional
    public void updateComic(Integer id, ComicAdminDTO form) {
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

        comic.getGenreComics().clear();
        if (form.getGenreIds() != null && !form.getGenreIds().isEmpty()) {
            for (Integer genreId : form.getGenreIds()) {
                Genres genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new EntityNotFoundException("Genre not found: " + genreId));
                GenreComics gc = GenreComics.builder()
                        .id(new GenreComicsId(comic.getComicId(), genreId))
                        .comic(comic)
                        .genre(genre)
                        .build();
                comic.getGenreComics().add(gc);
            }
        }
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
    @Transactional
    public void hardDeleteComic(Integer id) {
        Comics comic = comicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + id));
        comicRepository.delete(comic);
    }

    @Override
    public List<GenreAdminDTO> getAllGenresWithCount() {
        return genreRepository.findAll().stream()
                .map(g -> GenreAdminDTO.builder()
                        .genreId(g.getGenreId())
                        .name(g.getName())
                        .slug(g.getSlug())
                        .comicCount(g.getGenreComics().size())
                        .build())
                .sorted(Comparator.comparingLong(GenreAdminDTO::getComicCount).reversed())
                .toList();
    }

    // ── Legacy / other uses ──────────────────────────────────────────────────

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

    // ── Client-facing CRUD ───────────────────────────────────────────────────

    @Override
    public ComicDTO create(ComicDTO request) {
        Users user = usersRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check for duplicate title
        comicRepository.findByTitleIgnoreCase(request.getTitle())
                .ifPresent(comic -> {
                    throw new IllegalArgumentException("Comic with title '" + request.getTitle() + "' already exists");
                });

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

        Comics savedComic = comicRepository.save(comic);

        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            for (Integer genreId : request.getGenreIds()) {
                Genres genre = genreRepository.findById(genreId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Genre not found"));
                GenreComics gc = GenreComics.builder()
                        .id(new GenreComicsId(savedComic.getComicId(), genreId))
                        .comic(savedComic)
                        .genre(genre)
                        .build();
                savedComic.getGenreComics().add(gc);
            }
            comicRepository.save(savedComic);
        }

        return toDTO(savedComic);
    }

    @Override
    @Transactional(readOnly = true)
    public ComicDTO getById(Integer comicId) {
        return toDTO(findComicOrThrow(comicId));
    }

    @Override
    @Transactional(readOnly = true)
    public ComicDTO getBySlug(String slug) {
        return toDTO(comicRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found")));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicDTO> getAll() {
        return comicRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicDTO> getByUserId(Integer userId) {
        return comicRepository.findByUser_UserId(userId).stream().map(this::toDTO).toList();
    }

    @Override
    public ComicDTO update(Integer comicId, ComicDTO request) {
        Comics comic = findComicOrThrow(comicId);
        Users user = usersRepository.findById(request.getUserId())
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

        return toDTO(comicRepository.save(comic));
    }

    @Override
    public void delete(Integer comicId) {
        Comics comic = findComicOrThrow(comicId);
        comicRepository.delete(comic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicSummaryDTO> searchByTitle(String query) {
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
                .map(comic -> ComicSummaryDTO.builder()
                        .comicId(comic.getComicId())
                        .title(comic.getTitle())
                        .viewCount(comic.getViewCount())
                        .status(comic.getStatus())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComicDTO> searchForPageByTitle(String query) {
        String normalizedKeyword = query == null ? "" : query.trim();
        if (normalizedKeyword.isEmpty()) {
            return List.of();
        }

        return comicRepository.findByTitleContainingIgnoreCase(normalizedKeyword).stream()
                .sorted(Comparator.comparing(
                        Comics::getUpdatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(this::toDTO)
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Comics findComicOrThrow(Integer comicId) {
        return comicRepository.findById(comicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));
    }

    private ComicAdminDTO toComicAdminDTO(Comics comic) {
        String uploaderUsername = comic.getUser() != null ? comic.getUser().getUsername() : null;
        int chapterCount = comic.getChapters() != null ? comic.getChapters().size() : 0;
        List<Integer> genreIds = comic.getGenreComics() != null
                ? comic.getGenreComics().stream()
                        .filter(gc -> gc.getGenre() != null)
                        .map(gc -> gc.getGenre().getGenreId())
                        .toList()
                : List.of();
        return ComicAdminDTO.builder()
                .comicId(comic.getComicId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .description(comic.getDescription())
                .status(comic.getStatus())
                .viewCount(comic.getViewCount())
                .chapterCount(chapterCount)
                .uploaderUsername(uploaderUsername)
                .coverImg(comic.getCoverImg())
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .genreIds(genreIds)
                .build();
    }

    private ComicDTO toDTO(Comics comic) {
        return ComicDTO.builder()
                .comicId(comic.getComicId())
                .title(comic.getTitle())
                .slug(comic.getSlug())
                .description(comic.getDescription())
                .coverImg(comic.getCoverImg())
                .status(comic.getStatus())
                .viewCount(comic.getViewCount())
                .followerCount(comic.getBookmarks() != null ? comic.getBookmarks().size() : 0)
                .bookmarked(false)   // default false – controller sets the real value
                .userId(comic.getUser() != null ? comic.getUser().getUserId() : null)
                .authorName(comic.getUser() != null ? comic.getUser().getUsername() : null)
                .createdAt(comic.getCreatedAt())
                .updatedAt(comic.getUpdatedAt())
                .chapters(comic.getChapters() != null
                        ? comic.getChapters().stream()
                                .sorted(Comparator.comparing(ch -> ch.getChapterNumber() == null ? 0 : ch.getChapterNumber()))
                                .map(ch -> ComicDTO.ChapterSummary.builder()
                                        .chapterId(ch.getChapterId())
                                        .chapterNumber(ch.getChapterNumber())
                                        .title(ch.getTitle())
                                        .createdAt(ch.getCreatedAt())
                                        .build())
                                .toList()
                        : new ArrayList<>())
                .genres(comic.getGenreComics() != null
                        ? comic.getGenreComics().stream()
                                .filter(gc -> gc.getGenre() != null)
                                .map(gc -> ComicDTO.GenreSummary.builder()
                                        .genreId(gc.getGenre().getGenreId())
                                        .name(gc.getGenre().getName())
                                        .build())
                                .toList()
                        : new ArrayList<>())
                .build();
    }
}

