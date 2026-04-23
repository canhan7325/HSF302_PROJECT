package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.comic.ComicSummaryDTO;
import com.group1.mangaflowweb.dto.admin.DashboardStatsDTO;
import com.group1.mangaflowweb.dto.genre.GenreComicCountDTO;
import com.group1.mangaflowweb.dto.admin.RevenueDataPointDTO;

import java.util.List;

public interface AdminDashboardService {

    DashboardStatsDTO getDashboardStats();

    List<ComicSummaryDTO> getTop5MostViewedComics();

    List<ComicSummaryDTO> getComicsSortedByViewCount();

    List<GenreComicCountDTO> getComicsPerGenre();

    List<RevenueDataPointDTO> getRevenueByPeriod(String period);
}

