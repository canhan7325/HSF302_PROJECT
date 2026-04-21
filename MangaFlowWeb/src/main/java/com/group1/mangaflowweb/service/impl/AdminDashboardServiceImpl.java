package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.response.admin.ComicSummaryResponse;
import com.group1.mangaflowweb.dto.response.admin.DashboardStatsResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreComicCountResponse;
import com.group1.mangaflowweb.dto.response.admin.RevenueDataPointResponse;
import com.group1.mangaflowweb.enums.ComicEnum;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.GenreRepository;
import com.group1.mangaflowweb.repository.TransactionRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.AdminDashboardService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final ComicRepository comicRepository;
    private final GenreRepository genreRepository;
    private final TransactionRepository transactionRepository;

    public AdminDashboardServiceImpl(UserRepository userRepository,
                                     ComicRepository comicRepository,
                                     GenreRepository genreRepository,
                                     TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.comicRepository = comicRepository;
        this.genreRepository = genreRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public DashboardStatsResponse getDashboardStats() {
        long activeUsers = userRepository.countByEnabledTrue();
        long activeComics = comicRepository.countByStatusNot(ComicEnum.CANCELED);
        long totalViewCount = comicRepository.findAll().stream()
                .mapToLong(c -> c.getViewCount() != null ? c.getViewCount() : 0L)
                .sum();
        long activeGenres = genreRepository.count();
        return new DashboardStatsResponse(activeUsers, activeComics, totalViewCount, activeGenres);
    }

    @Override
    public List<ComicSummaryResponse> getTop5MostViewedComics() {
        return comicRepository.findTop5ByStatusNotOrderByViewCountDesc(ComicEnum.CANCELED)
                .stream()
                .map(c -> new ComicSummaryResponse(c.getComicId(), c.getTitle(), c.getViewCount(), c.getStatus()))
                .toList();
    }

    @Override
    public List<ComicSummaryResponse> getComicsSortedByViewCount() {
        return comicRepository.findByStatusNotOrderByViewCountDesc(ComicEnum.CANCELED)
                .stream()
                .map(c -> new ComicSummaryResponse(c.getComicId(), c.getTitle(), c.getViewCount(), c.getStatus()))
                .toList();
    }

    @Override
    public List<GenreComicCountResponse> getComicsPerGenre() {
        return genreRepository.findAll().stream()
                .map(g -> new GenreComicCountResponse(g.getGenreId(), g.getName(), g.getComics().size()))
                .sorted(Comparator.comparingLong(GenreComicCountResponse::comicCount).reversed())
                .toList();
    }

    @Override
    public List<RevenueDataPointResponse> getRevenueByPeriod(String period) {
        List<Object[]> rows;
        if ("week".equals(period)) {
            LocalDateTime since = LocalDateTime.now().minusWeeks(12);
            rows = transactionRepository.findRevenueByWeek(since);
        } else if ("year".equals(period)) {
            rows = transactionRepository.findRevenueByYear();
        } else {
            // "month" — show all months across all years so historical data is visible
            LocalDateTime since = LocalDateTime.of(2000, 1, 1, 0, 0);
            rows = transactionRepository.findRevenueByMonth(since);
        }
        return rows.stream()
                .map(row -> new RevenueDataPointResponse(
                        (String) row[0],
                        row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO))
                .toList();
    }
}
