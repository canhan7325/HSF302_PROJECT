package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.genre.GenreDTO;
import com.group1.mangaflowweb.dto.genre.GenreAdminDTO;
import com.group1.mangaflowweb.entity.Genres;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.service.GenreService;
import com.group1.mangaflowweb.util.SlugUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class GenreServiceImpl implements GenreService {

    private final GenreRepository genreRepository;

    public GenreServiceImpl(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
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

    @Override
    public List<GenreAdminDTO> searchGenres(String name) {
        return genreRepository.findByNameContainingIgnoreCase(name).stream()
                .map(g -> GenreAdminDTO.builder()
                        .genreId(g.getGenreId())
                        .name(g.getName())
                        .slug(g.getSlug())
                        .comicCount(g.getGenreComics().size())
                        .build())
                .sorted(Comparator.comparingLong(GenreAdminDTO::getComicCount).reversed())
                .toList();
    }

    @Override
    @Transactional
    public void createGenre(GenreDTO form) {
        genreRepository.findByName(form.getName()).ifPresent(g -> {
            throw new IllegalArgumentException("Genre name already exists: " + form.getName());
        });
        Genres genre = new Genres();
        genre.setName(form.getName());
        genre.setSlug(SlugUtils.toSlug(form.getName()));
        genreRepository.save(genre);
    }

    @Override
    @Transactional
    public void updateGenre(Integer id, GenreDTO form) {
        Genres genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
        genre.setName(form.getName());
        genre.setSlug(SlugUtils.toSlug(form.getName()));
        genreRepository.save(genre);
    }

    @Override
    @Transactional
    public void deleteGenre(Integer id) {
        Genres genre = genreRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Genre not found with id: " + id));
        if (!genre.getGenreComics().isEmpty()) {
            throw new IllegalStateException("Cannot delete genre with assigned comics: " + genre.getName());
        }
        genreRepository.deleteById(id);
    }

    @Override
    public List<GenreDTO> getAllGenres() {
        return genreRepository.findAll()
                .stream()
                .map(g -> GenreDTO.builder()
                        .genreId(g.getGenreId())
                        .name(g.getName())
                        .build())
                .toList();
    }
}

