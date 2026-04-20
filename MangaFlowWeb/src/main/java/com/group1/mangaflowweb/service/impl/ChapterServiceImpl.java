package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.ChapterRequest;
import com.group1.mangaflowweb.dto.response.ChapterAdminResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.service.ChapterService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;

    public ChapterServiceImpl(ChapterRepository chapterRepository, ComicRepository comicRepository) {
        this.chapterRepository = chapterRepository;
        this.comicRepository = comicRepository;
    }

    @Override
    public List<ChapterAdminResponse> getChaptersByComic(Integer comicId) {
        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + comicId));
        return chapterRepository.findByComicOrderByChapterNumberDesc(comic)
                .stream()
                .map(ch -> new ChapterAdminResponse(
                        ch.getChapterId(),
                        ch.getChapterNumber(),
                        ch.getTitle(),
                        ch.getPages() != null ? ch.getPages().size() : 0,
                        ch.getCreatedAt()))
                .toList();
    }

    @Override
    public ChapterAdminResponse getChapterById(Integer chapterId) {
        Chapters ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id: " + chapterId));
        return new ChapterAdminResponse(ch.getChapterId(), ch.getChapterNumber(), ch.getTitle(),
                ch.getPages() != null ? ch.getPages().size() : 0, ch.getCreatedAt());
    }

    @Override
    @Transactional
    public void createChapter(Integer comicId, ChapterRequest form) {
        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + comicId));
        Chapters chapter = new Chapters();
        chapter.setChapterNumber(form.getChapterNumber());
        chapter.setTitle(form.getTitle());
        chapter.setComic(comic);
        chapterRepository.save(chapter);
    }

    @Override
    @Transactional
    public void updateChapter(Integer chapterId, ChapterRequest form) {
        Chapters chapter = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id: " + chapterId));
        chapter.setTitle(form.getTitle());
        chapterRepository.save(chapter);
    }

    @Override
    @Transactional
    public void deleteChapter(Integer chapterId) {
        if (!chapterRepository.existsById(chapterId)) {
            throw new EntityNotFoundException("Chapter not found with id: " + chapterId);
        }
        chapterRepository.deleteById(chapterId);
    }
}
