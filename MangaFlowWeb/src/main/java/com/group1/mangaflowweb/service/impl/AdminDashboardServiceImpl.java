package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.comic.ComicSummaryDTO;
import com.group1.mangaflowweb.dto.admin.DashboardStatsDTO;
import com.group1.mangaflowweb.dto.genre.GenreComicCountDTO;
import com.group1.mangaflowweb.dto.admin.RevenueDataPointDTO;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.repository.TransactionsRepository;
import com.group1.mangaflowweb.repository.UsersRepository;
import com.group1.mangaflowweb.service.AdminDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UsersRepository usersRepository;
    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final TransactionsRepository transactionsRepository;

    public AdminDashboardServiceImpl(UsersRepository usersRepository,
                                     ComicRepository comicRepository,
                                     GenreRepository genreRepository,
                                     TransactionsRepository transactionsRepository) {
        this.usersRepository = usersRepository;
        this.comicRepository = comicRepository;
        this.genreRepository = genreRepository;
        this.transactionsRepository = transactionsRepository;
    }

    @Override
    public DashboardStatsDTO getDashboardStats() {
        long activeUsers = usersRepository.countByEnabledTrue();
        long activeComics = comicRepository.countByStatusNot(ComicEnum.CANCELED);
        long totalViewCount = comicRepository.findAll().stream()
                .mapToLong(c -> c.getViewCount() != null ? c.getViewCount() : 0L)
                .sum();
        long activeGenres = genreRepository.count();
        return DashboardStatsDTO.builder()
                .totalActiveUsers(activeUsers)
                .totalActiveComics(activeComics)
                .totalViewCount(totalViewCount)
                .totalActiveGenres(activeGenres)
                .build();
    }

    @Override
    public List<ComicSummaryDTO> getTop5MostViewedComics() {
        return comicRepository.findTop5ByStatusNotOrderByViewCountDesc(ComicEnum.CANCELED)
                .stream()
                .map(c -> ComicSummaryDTO.builder()
                        .comicId(c.getComicId())
                        .title(c.getTitle())
                        .viewCount(c.getViewCount())
                        .status(c.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<ComicSummaryDTO> getComicsSortedByViewCount() {
        return comicRepository.findByStatusNotOrderByViewCountDesc(ComicEnum.CANCELED)
                .stream()
                .map(c -> ComicSummaryDTO.builder()
                        .comicId(c.getComicId())
                        .title(c.getTitle())
                        .viewCount(c.getViewCount())
                        .status(c.getStatus())
                        .build())
                .toList();
    }

    @Override
    public List<GenreComicCountDTO> getComicsPerGenre() {
        return genreRepository.findAll().stream()
                .map(g -> GenreComicCountDTO.builder()
                        .genreId(g.getGenreId())
                        .genreName(g.getName())
                        .comicCount(g.getGenreComics().size())
                        .build())
                .sorted(Comparator.comparingLong(GenreComicCountDTO::getComicCount).reversed())
                .toList();
    }

    @Override
    public List<RevenueDataPointDTO> getRevenueByPeriod(String period) {
        List<Object[]> rows;
        if ("week".equals(period)) {
            LocalDateTime since = LocalDateTime.now().minusWeeks(12);
            rows = transactionsRepository.findRevenueByWeek(since);
        } else if ("year".equals(period)) {
            rows = transactionsRepository.findRevenueByYear();
        } else {
            LocalDateTime since = LocalDateTime.of(2000, 1, 1, 0, 0);
            rows = transactionsRepository.findRevenueByMonth(since);
        }
        return rows.stream()
                .map(row -> RevenueDataPointDTO.builder()
                        .period((String) row[0])
                        .revenue(row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO)
                        .build())
                .toList();
    }
}

