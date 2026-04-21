package com.group1.mangaflowweb.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ImageUrlResolver {

    private final String cloudinaryBaseUrl;

    public ImageUrlResolver(@Value("${cloudinary.base-url:}") String cloudinaryBaseUrl) {
        this.cloudinaryBaseUrl = trimTrailingSlash(cloudinaryBaseUrl);
    }

    public String resolve(String rawPath) {
        if (rawPath == null) {
            return "";
        }

        String path = rawPath.trim();
        if (path.isEmpty()) {
            return "";
        }

        if (isAbsoluteUrl(path)) {
            return path;
        }

        String normalizedPath = stripLegacyUploadPrefix(path);
        if (cloudinaryBaseUrl.isBlank()) {
            return normalizedPath;
        }

        return cloudinaryBaseUrl + "/f_auto,q_auto/" + stripLeadingSlash(normalizedPath);
    }

    public String normalizeForStorage(String rawPath) {
        if (rawPath == null) {
            return null;
        }
        String path = rawPath.trim();
        if (path.isEmpty() || isAbsoluteUrl(path)) {
            return path;
        }

        path = stripLegacyUploadPrefix(path);
        return stripLeadingSlash(path);
    }

    private boolean isAbsoluteUrl(String value) {
        return value.startsWith("http://") || value.startsWith("https://");
    }

    private String stripLegacyUploadPrefix(String value) {
        if (value.startsWith("/uploads/")) {
            return value.substring("/uploads/".length());
        }
        if (value.startsWith("uploads/")) {
            return value.substring("uploads/".length());
        }
        return value;
    }

    private String stripLeadingSlash(String value) {
        if (value.startsWith("/")) {
            return value.substring(1);
        }
        return value;
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
