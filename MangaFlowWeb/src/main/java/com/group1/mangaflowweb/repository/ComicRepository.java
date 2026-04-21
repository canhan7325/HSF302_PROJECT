package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComicRepository extends JpaRepository<Comics, Integer> {
	@Query("SELECT DISTINCT c FROM Comics c " +
			"LEFT JOIN FETCH c.user " +
			"LEFT JOIN FETCH c.genreComics gc " +
			"LEFT JOIN FETCH gc.genre " +
			"LEFT JOIN FETCH c.chapters " +
			"WHERE c.slug = :slug")
	Optional<Comics> findBySlug(String slug);

	@Query("SELECT DISTINCT c FROM Comics c " +
			"LEFT JOIN FETCH c.user " +
			"LEFT JOIN FETCH c.genreComics gc " +
			"LEFT JOIN FETCH gc.genre " +
			"ORDER BY c.viewCount DESC")
	List<Comics> findTop6ByOrderByViewCountDesc();

	@Query("SELECT DISTINCT c FROM Comics c " +
			"LEFT JOIN FETCH c.user " +
			"LEFT JOIN FETCH c.genreComics gc " +
			"LEFT JOIN FETCH gc.genre " +
			"ORDER BY c.updatedAt DESC")
	Page<Comics> findAllByOrderByUpdatedAtDesc(Pageable pageable);

	List<Comics> findByUser_UserId(Integer userId);
}
