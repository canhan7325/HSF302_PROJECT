package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ComicRepository extends JpaRepository<Comics, Integer> {
	List<Comics> findTop6ByOrderByViewCountDesc();

	Page<Comics> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
