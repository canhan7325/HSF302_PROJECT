package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.ReadingHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingHistoryRepository extends JpaRepository<ReadingHistories, Integer> {

    // newest first
    List<ReadingHistories> findByUser_UserIdOrderByReadAtDesc(Integer userId);
}
