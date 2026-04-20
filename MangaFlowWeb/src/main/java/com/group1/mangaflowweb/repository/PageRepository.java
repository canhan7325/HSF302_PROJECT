package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Pages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PageRepository extends JpaRepository<Pages, Integer> {
}
