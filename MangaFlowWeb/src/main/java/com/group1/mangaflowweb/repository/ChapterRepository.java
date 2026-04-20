package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapters, Integer> {
    List<Chapters> findByComicOrderByChapterNumberAsc(Comics comic);
    List<Chapters> findByComicOrderByChapterNumberDesc(Comics comic);
}
