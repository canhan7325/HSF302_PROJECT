package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.ReadingHistories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReadingHistoryRepository extends JpaRepository<ReadingHistories, Integer> {

    @Query(value = """
        SELECT c.title,
               CAST(rh.read_at AS DATE) AS bucket,
               COUNT(*)                 AS readCount
        FROM   reading_history rh
        JOIN   chapters ch ON ch.chapter_id = rh.chapter_id
        JOIN   comics   c  ON c.comic_id    = ch.comic_id
        WHERE  rh.read_at >= :since
          AND  c.comic_id IN (
                   SELECT TOP (:topN) ch2.comic_id
                   FROM   reading_history rh2
                   JOIN   chapters ch2 ON ch2.chapter_id = rh2.chapter_id
                   WHERE  rh2.read_at >= :since
                   GROUP  BY ch2.comic_id
                   ORDER  BY COUNT(*) DESC
               )
        GROUP  BY c.comic_id, c.title, CAST(rh.read_at AS DATE)
        ORDER  BY c.title, bucket
        """, nativeQuery = true)
    List<Object[]> findTopComicsTimeSeriesDay(@Param("since") LocalDateTime since,
                                              @Param("topN")  int topN);

    @Query(value = """
        SELECT c.title,
               CAST(DATEPART(iso_week, rh.read_at) AS VARCHAR) AS bucket,
               COUNT(*)                                         AS readCount
        FROM   reading_history rh
        JOIN   chapters ch ON ch.chapter_id = rh.chapter_id
        JOIN   comics   c  ON c.comic_id    = ch.comic_id
        WHERE  rh.read_at >= :since
          AND  c.comic_id IN (
                   SELECT TOP (:topN) ch2.comic_id
                   FROM   reading_history rh2
                   JOIN   chapters ch2 ON ch2.chapter_id = rh2.chapter_id
                   WHERE  rh2.read_at >= :since
                   GROUP  BY ch2.comic_id
                   ORDER  BY COUNT(*) DESC
               )
        GROUP  BY c.comic_id, c.title, DATEPART(iso_week, rh.read_at)
        ORDER  BY c.title, bucket
        """, nativeQuery = true)
    List<Object[]> findTopComicsTimeSeriesWeek(@Param("since") LocalDateTime since,
                                               @Param("topN")  int topN);

    @Query(value = """
        SELECT c.title,
               FORMAT(rh.read_at, 'yyyy-MM') AS bucket,
               COUNT(*)                       AS readCount
        FROM   reading_history rh
        JOIN   chapters ch ON ch.chapter_id = rh.chapter_id
        JOIN   comics   c  ON c.comic_id    = ch.comic_id
        WHERE  rh.read_at >= :since
          AND  c.comic_id IN (
                   SELECT TOP (:topN) ch2.comic_id
                   FROM   reading_history rh2
                   JOIN   chapters ch2 ON ch2.chapter_id = rh2.chapter_id
                   WHERE  rh2.read_at >= :since
                   GROUP  BY ch2.comic_id
                   ORDER  BY COUNT(*) DESC
               )
        GROUP  BY c.comic_id, c.title, FORMAT(rh.read_at, 'yyyy-MM')
        ORDER  BY c.title, bucket
        """, nativeQuery = true)
    List<Object[]> findTopComicsTimeSeriesYear(@Param("since") LocalDateTime since,
                                               @Param("topN")  int topN);
}
