package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterResponse;
import com.group1.mangaflowweb.dto.page.PageResponse;
import com.group1.mangaflowweb.dto.request.admin.ChapterAdDTO;
import com.group1.mangaflowweb.dto.response.admin.ChapterAdminResponse;
import com.group1.mangaflowweb.dto.response.admin.PageAdminResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final PageRepository pageRepository;
    private final ImageUrlResolver imageUrlResolver;

    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              ComicRepository comicRepository,
                              PageRepository pageRepository,
                              ImageUrlResolver imageUrlResolver) {
        this.chapterRepository = chapterRepository;
        this.comicRepository = comicRepository;
        this.pageRepository = pageRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    @Override
    public List<ChapterAdminResponse> getChaptersByComic(Integer comicId) {
        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + comicId));
        return chapterRepository.findByComicOrderByChapterNumberDesc(comic)
                .stream()
                .map(ch -> {
                    List<PageAdminResponse> pages =
                        pageRepository.findByChapterChapterIdOrderByPageNumberAsc(ch.getChapterId())
                            .stream()
                            .map(p -> new PageAdminResponse(p.getPageId(), p.getPageNumber(),
                                    imageUrlResolver.resolve(p.getImgPath())))
                            .toList();
                    return new ChapterAdminResponse(
                            ch.getChapterId(), ch.getChapterNumber(), ch.getTitle(),
                            pages.size(), ch.getCreatedAt(), pages);
                })
                .toList();
    }

    @Override
    public ChapterAdminResponse getChapterById(Integer chapterId) {
        Chapters ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id: " + chapterId));
        List<PageAdminResponse> pages =
            pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(p -> new PageAdminResponse(p.getPageId(), p.getPageNumber(),
                        imageUrlResolver.resolve(p.getImgPath())))
                .toList();
        return new ChapterAdminResponse(ch.getChapterId(), ch.getChapterNumber(), ch.getTitle(),
                pages.size(), ch.getCreatedAt(), pages);
    }

    @Override
    @Transactional
    public void createChapter(Integer comicId, ChapterAdDTO form) {
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
    public void updateChapter(Integer chapterId, ChapterAdDTO form) {
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

    @Override
    public ChapterResponse create(ChapterDTO request) {
        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));
        chapterRepository.findByComic_ComicIdAndChapterNumber(request.getComicId(), request.getChapterNumber())
                .ifPresent(ch -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Chapter number already exists"); });
        Chapters chapter = Chapters.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .build();
        return toResponse(chapterRepository.save(chapter));
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterResponse getById(Integer chapterId) {
        return toResponse(findChapterOrThrow(chapterId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getAll() {
        return chapterRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterResponse> getByComicId(Integer comicId) {
        return chapterRepository.findByComic_ComicId(comicId).stream().map(this::toResponse).toList();
    }

    @Override
    public ChapterResponse update(Integer chapterId, ChapterDTO request) {
        Chapters chapter = findChapterOrThrow(chapterId);
        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));
        chapterRepository.findByComic_ComicIdAndChapterNumber(request.getComicId(), request.getChapterNumber())
                .filter(existing -> !existing.getChapterId().equals(chapterId))
                .ifPresent(existing -> { throw new ResponseStatusException(HttpStatus.CONFLICT, "Chapter number already exists"); });
        chapter.setComic(comic);
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setTitle(request.getTitle());
        return toResponse(chapterRepository.save(chapter));
    }

    @Override
    public void delete(Integer chapterId) {
        chapterRepository.delete(findChapterOrThrow(chapterId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageResponse> getAllPageByChapterId(Integer chapterId) {
        findChapterOrThrow(chapterId);
        return pageRepository.findByChapter_ChapterId(chapterId)
                .stream()
                .map(p -> PageResponse.builder()
                        .pageId(p.getPageId())
                        .chapterId(p.getChapter() != null ? p.getChapter().getChapterId() : null)
                        .pageNumber(p.getPageNumber())
                        .imgPath(p.getImgPath())
                        .build())
                .toList();
    }

    private Chapters findChapterOrThrow(Integer chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));
    }

    private ChapterResponse toResponse(Chapters chapter) {
        return ChapterResponse.builder()
                .chapterId(chapter.getChapterId())
                .comicId(chapter.getComic() != null ? chapter.getComic().getComicId() : null)
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .createdAt(chapter.getCreatedAt())
                .pages(null)
                .build();
    }
}
