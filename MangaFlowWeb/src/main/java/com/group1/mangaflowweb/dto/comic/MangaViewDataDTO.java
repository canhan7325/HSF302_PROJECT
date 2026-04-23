package com.group1.mangaflowweb.dto.comic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * One entry per manga — carries the full time-series so the frontend
 * can draw one line per manga on a Chart.js line chart.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MangaViewDataDTO {
    private String title;
    private List<String> labels;   // x-axis buckets (date strings)
    private List<Long> data;       // read counts per bucket
}
