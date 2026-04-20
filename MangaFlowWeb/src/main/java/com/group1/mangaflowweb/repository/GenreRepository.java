package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Genres;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genres, Integer> {
    Optional<Genres> findByName(String name);
    List<Genres> findByNameContainingIgnoreCase(String name);
}