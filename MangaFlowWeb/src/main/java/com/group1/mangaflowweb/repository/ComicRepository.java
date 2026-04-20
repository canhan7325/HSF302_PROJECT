package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ComicRepository extends JpaRepository<Comics, Integer> {
    List<Comics> findTop5ByStatusNotOrderByViewCountDesc(ComicEnum status);
    List<Comics> findByStatusNotOrderByViewCountDesc(ComicEnum status);
    long countByStatusNot(ComicEnum status);
    Page<Comics> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Comics> findByTitleContainingIgnoreCase(String title);
    Optional<Comics> findBySlug(String slug);
}