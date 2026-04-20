package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comics, Integer> {
    Optional<Comics> findBySlug(String slug);

    List<Comics> findByUser_UserId(Integer userId);
}
