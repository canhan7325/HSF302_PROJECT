package com.group1.mangaflowweb.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.group1.mangaflowweb.service.CloudinaryUploadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryUploadServiceImpl implements CloudinaryUploadService {

    private final Cloudinary cloudinary;

    /** If false, we don't attempt Cloudinary upload and callers should fallback to disk. */
    private final boolean enabled;

    public CloudinaryUploadServiceImpl(Cloudinary cloudinary,
                                       @Value("${cloudinary.enabled:true}") boolean enabled) {
        this.cloudinary = cloudinary;
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled && cloudinary != null;
    }

    @Override
    public String uploadImage(MultipartFile file, String publicId) throws IOException {
        if (!isEnabled()) {
            throw new IOException("Cloudinary upload disabled");
        }
        if (file == null || file.isEmpty()) {
            throw new IOException("Empty file");
        }

        Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "resource_type", "image",
                "overwrite", true
        ));

        Object storedId = result.get("public_id");
        return storedId != null ? storedId.toString() : publicId;
    }
}
