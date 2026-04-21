package com.group1.mangaflowweb.dto.request.admin;

import jakarta.validation.constraints.NotBlank;

public class GenreRequest {

    @NotBlank
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
