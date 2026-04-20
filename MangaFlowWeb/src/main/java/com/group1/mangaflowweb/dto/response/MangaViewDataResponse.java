package com.group1.mangaflowweb.dto.response;

import java.util.List;

/**
 * One entry per manga — carries the full time-series so the frontend
 * can draw one line per manga on a Chart.js line chart.
 */
public record MangaViewDataResponse(
        String title,
        List<String> labels,   // x-axis buckets (date strings)
        List<Long>   data      // read counts per bucket
) {}
