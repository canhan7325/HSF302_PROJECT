package com.group1.mangaflowweb.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageAdminResponse {
    private Integer pageId;
    private Integer pageNumber;
    private String imgPath;
}
