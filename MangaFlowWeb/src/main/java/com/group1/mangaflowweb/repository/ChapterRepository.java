package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Chapters;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterRepository extends JpaRepository<Chapters, Integer> {
}
