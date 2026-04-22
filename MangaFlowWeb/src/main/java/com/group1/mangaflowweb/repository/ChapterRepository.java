package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface ChapterRepository extends JpaRepository<Chapters, Integer> {
    List<Chapters> findByComicOrderByChapterNumberAsc(Comics comic);
    List<Chapters> findByComicOrderByChapterNumberDesc(Comics comic);
    Optional<Chapters> findByComic_ComicIdAndChapterNumber(Integer comicId, Integer chapterNumber);

    List<Chapters> findByComic_ComicId(Integer comicId);
    List<Chapters> findByComic_ComicIdOrderByChapterNumberDesc(Integer comicId);
    Optional<Chapters> findFirstByComic_ComicIdOrderByChapterNumberAsc(Integer comicId);
}
