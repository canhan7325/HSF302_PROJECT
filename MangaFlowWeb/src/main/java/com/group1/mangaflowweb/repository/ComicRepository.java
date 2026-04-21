package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.enums.ComicEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;

@Repository
public interface ComicRepository extends JpaRepository<Comics, Integer> {
	List<Comics> findTop5ByStatusNotOrderByViewCountDesc(ComicEnum status);

	List<Comics> findByStatusNotOrderByViewCountDesc(ComicEnum status);

	long countByStatusNot(ComicEnum status);

	Page<Comics> findByTitleContainingIgnoreCase(String title, Pageable pageable);

	List<Comics> findByTitleContainingIgnoreCase(String title);


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