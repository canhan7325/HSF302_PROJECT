package com.group1.mangaflowweb.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Uploads images to Cloudinary and returns the stored public_id (without extension).
 * The public_id is compatible with ImageUrlResolver.resolve().
 */
public interface CloudinaryUploadService {

    /**
     * @param file image file
     * @param publicId desired Cloudinary public_id (e.g. "comics/naruto/cover_abc123")
     * @return actual public_id stored in Cloudinary
     */
    String uploadImage(MultipartFile file, String publicId) throws IOException;

    boolean isEnabled();
}

