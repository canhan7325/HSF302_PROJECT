package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ComicRepository extends JpaRepository<Comics, Integer> {
    List<Comics> findByTitleContainingIgnoreCase(String title);
    Optional<Comics> findBySlug(String slug);
}




