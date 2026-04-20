package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.response.ComicSummaryResponse;
import com.group1.mangaflowweb.dto.response.DashboardStatsResponse;
import com.group1.mangaflowweb.dto.response.GenreComicCountResponse;
import com.group1.mangaflowweb.dto.response.RevenueDataPointResponse;

import java.util.List;

public interface AdminDashboardService {

    DashboardStatsResponse getDashboardStats();

    List<ComicSummaryResponse> getTop5MostViewedComics();

    List<ComicSummaryResponse> getComicsSortedByViewCount();

    List<GenreComicCountResponse> getComicsPerGenre();

    List<RevenueDataPointResponse> getRevenueByPeriod(String period);
}
