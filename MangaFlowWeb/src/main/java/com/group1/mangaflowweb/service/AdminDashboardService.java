package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.response.admin.ComicSummaryResponse;
import com.group1.mangaflowweb.dto.response.admin.DashboardStatsResponse;
import com.group1.mangaflowweb.dto.response.admin.GenreComicCountResponse;
import com.group1.mangaflowweb.dto.response.admin.RevenueDataPointResponse;

import java.util.List;

public interface AdminDashboardService {

    DashboardStatsResponse getDashboardStats();

    List<ComicSummaryResponse> getTop5MostViewedComics();

    List<ComicSummaryResponse> getComicsSortedByViewCount();

    List<GenreComicCountResponse> getComicsPerGenre();

    List<RevenueDataPointResponse> getRevenueByPeriod(String period);
}
