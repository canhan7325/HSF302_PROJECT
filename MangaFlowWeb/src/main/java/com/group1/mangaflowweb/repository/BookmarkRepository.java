package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Bookmarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmarks, Integer> {
    Optional<Bookmarks> findByUser_UserIdAndComic_ComicId(Integer userId, Integer comicId);

    List<Bookmarks> findByUser_UserId(Integer userId);

    List<Bookmarks> findByComic_ComicId(Integer comicId);
}
