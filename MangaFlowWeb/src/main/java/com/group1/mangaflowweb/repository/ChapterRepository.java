package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Chapters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChapterRepository extends JpaRepository<Chapters, Integer> {
    Optional<Chapters> findByComic_ComicIdAndChapterNumber(Integer comicId, Integer chapterNumber);

    List<Chapters> findByComic_ComicId(Integer comicId);
}
