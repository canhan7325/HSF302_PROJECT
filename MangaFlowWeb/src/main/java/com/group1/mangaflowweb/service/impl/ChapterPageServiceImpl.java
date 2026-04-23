package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.service.ChapterPageService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import org.springframework.stereotype.Service;
import com.group1.mangaflowweb.entity.Chapters;

import java.util.Comparator;
import java.util.List;

@Service
public class ChapterPageServiceImpl implements ChapterPageService {

    private final ChapterRepository chapterRepository;
    private final ImageUrlResolver imageUrlResolver;

    public ChapterPageServiceImpl(ChapterRepository chapterRepository, ImageUrlResolver imageUrlResolver) {
        this.chapterRepository = chapterRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    @Override
    public List<String> getPageImageUrls(Long chapterId) {
        if (chapterId == null) throw new IllegalArgumentException("chapterId is null");
        if (chapterId > Integer.MAX_VALUE || chapterId < Integer.MIN_VALUE) {
            throw new IllegalArgumentException("chapterId out of Integer range: " + chapterId);
        }

        Integer id = chapterId.intValue();

        Chapters chapter = chapterRepository.findById(id).orElseThrow();
        return chapter.getPages().stream()
                .sorted(Comparator.comparing(p -> p.getPageNumber())) // 👈 thêm dòng này
                .map(p -> imageUrlResolver.resolve(p.getImgPath()))
                .toList();
    }
}