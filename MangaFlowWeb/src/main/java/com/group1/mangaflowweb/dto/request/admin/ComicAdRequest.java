package com.group1.mangaflowweb.dto.request.admin;

import com.group1.mangaflowweb.enums.ComicEnum;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public class ComicAdRequest {

    @NotBlank
    private String title;

    private String description;

    private String coverImg;

    private ComicEnum status;

    private List<Integer> genreIds;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCoverImg() { return coverImg; }
    public void setCoverImg(String coverImg) { this.coverImg = coverImg; }

    public ComicEnum getStatus() { return status; }
    public void setStatus(ComicEnum status) { this.status = status; }

    public List<Integer> getGenreIds() { return genreIds; }
    public void setGenreIds(List<Integer> genreIds) { this.genreIds = genreIds; }
}
