package com.group1.mangaflowweb.service;

import java.io.IOException;

public interface ComicDownloadService {
    /**
     * Downloads all chapters of a comic, applies invisible watermark with userId,
     * and returns a ZIP file as byte array.
     *
     * @param comicId Comic ID
     * @param userId  User ID for watermark
     * @return ZIP file bytes
     * @throws IOException if download or processing fails
     */
    byte[] downloadComicWithWatermark(Integer comicId, Integer userId) throws IOException;
}
