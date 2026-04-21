package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Bookmarks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmarks, Integer> {
	long countByComic_ComicId(Integer comicId);

	boolean existsByUser_UserIdAndComic_ComicId(Integer userId, Integer comicId);
    List<Bookmarks> findByUser_UserId(Integer userId);

    List<Bookmarks> findByComic_ComicId(Integer comicId);
	Optional<Bookmarks> findByUser_UserIdAndComic_ComicId(Integer userId, Integer comicId);
}
