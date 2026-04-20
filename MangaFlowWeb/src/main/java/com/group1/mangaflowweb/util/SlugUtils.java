package com.group1.mangaflowweb.util;

public class SlugUtils {

    private SlugUtils() {}

    public static String toSlug(String name) {
        return name.trim().toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9\\-]", "");
    }
}
