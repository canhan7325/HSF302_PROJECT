package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Pages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<Pages, Integer> {
    Optional<Pages> findByChapter_ChapterIdAndPageNumber(Integer chapterId, Integer pageNumber);

    List<Pages> findByChapter_ChapterId(Integer chapterId);
    List<Pages> findByChapterChapterIdOrderByPageNumberAsc(Integer chapterId);
}
