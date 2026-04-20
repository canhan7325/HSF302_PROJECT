package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Genres;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genres, Integer> {
}
