package com.group1.mangaflowweb.dto.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageAdminDTO {
    private Integer pageId;
    private Integer pageNumber;
    private String imgPath;
}
