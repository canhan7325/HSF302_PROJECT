package com.group1.mangaflowweb.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalActiveUsers;
    private long totalActiveComics;
    private long totalViewCount;
    private long totalActiveGenres;
}
