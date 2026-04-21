package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.ReadingHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistories, Integer> {

    // newest first
    List<ReadingHistories> findByUser_UserIdOrderByReadAtDesc(Integer userId);

    // newest reading record of a user within a comic
    Optional<ReadingHistories> findFirstByUser_UserIdAndChapter_Comic_ComicIdOrderByReadAtDesc(Integer userId, Integer comicId);

    boolean existsByUser_UserIdAndChapter_Comic_ComicId(Integer userId, Integer comicId);
}
