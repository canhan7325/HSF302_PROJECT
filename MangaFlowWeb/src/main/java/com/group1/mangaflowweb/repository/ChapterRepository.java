package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Chapters;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapters, Integer> {
	List<Chapters> findByComic_ComicIdOrderByChapterNumberDesc(Integer comicId);
}
