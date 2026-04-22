package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.entity.Users;

public interface AccessService {

    @Deprecated
    boolean canReadFullChapter(Users user);

    ChapterAccess getChapterAccess(Users user);

    class ChapterAccess {
        private final boolean canReadFull;
        private final int previewCount;

        public ChapterAccess(boolean canReadFull, int previewCount) {
            this.canReadFull = canReadFull;
            this.previewCount = previewCount;
        }

        public boolean isCanReadFull() {
            return canReadFull;
        }

        public int getPreviewCount() {
            return previewCount;
        }
    }
}
