package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComicRepository extends JpaRepository<Comics, Integer> {
}
