package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Pages;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PageRepository extends JpaRepository<Pages, Integer> {
    List<Pages> findByChapterChapterIdOrderByPageNumberAsc(Integer chapterId);
}
