package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.chapter.ChapterAdminDTO;
import com.group1.mangaflowweb.dto.chapter.ChapterDTO;
import com.group1.mangaflowweb.dto.page.PageAdminDTO;
import com.group1.mangaflowweb.dto.page.PageDTO;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.PageRepository;
import com.group1.mangaflowweb.service.ChapterService;
import com.group1.mangaflowweb.util.ImageUrlResolver;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ChapterServiceImpl implements ChapterService {

    private final ChapterRepository chapterRepository;
    private final ComicRepository comicRepository;
    private final ImageUrlResolver imageUrlResolver;

    @Autowired
    private PageRepository pageRepository;

    public ChapterServiceImpl(ChapterRepository chapterRepository,
                              ComicRepository comicRepository,
                              ImageUrlResolver imageUrlResolver) {
        this.chapterRepository = chapterRepository;
        this.comicRepository = comicRepository;
        this.imageUrlResolver = imageUrlResolver;
    }

    // ── Admin methods ────────────────────────────────────────────────────────

    @Override
    public List<ChapterAdminDTO> getChaptersByComic(Integer comicId) {
        Comics comic = comicRepository.findById(comicId)
                .orElseThrow(() -> new EntityNotFoundException("Comic not found with id: " + comicId));
        return chapterRepository.findByComicOrderByChapterNumberDesc(comic)
                .stream()
                .map(ch -> {
                    List<PageAdminDTO> pages =
                        pageRepository.findByChapterChapterIdOrderByPageNumberAsc(ch.getChapterId())
                            .stream()
                            .map(p -> PageAdminDTO.builder()
                                    .pageId(p.getPageId())
                                    .pageNumber(p.getPageNumber())
                                    .imgPath(imageUrlResolver.resolve(p.getImgPath()))
                                    .build())
                            .toList();
                    return ChapterAdminDTO.builder()
                            .chapterId(ch.getChapterId())
                            .chapterNumber(ch.getChapterNumber())
                            .title(ch.getTitle())
                            .pageCount(pages.size())
                            .createdAt(ch.getCreatedAt())
                            .pages(pages)
                            .build();
                })
                .toList();
    }

    @Override
    public ChapterAdminDTO getChapterById(Integer chapterId) {
        Chapters ch = chapterRepository.findById(chapterId)
                .orElseThrow(() -> new EntityNotFoundException("Chapter not found with id: " + chapterId));
        List<PageAdminDTO> pages =
            pageRepository.findByChapterChapterIdOrderByPageNumberAsc(chapterId)
                .stream()
                .map(p -> PageAdminDTO.builder()
                        .pageId(p.getPageId())
                        .pageNumber(p.getPageNumber())
                        .imgPath(imageUrlResolver.resolve(p.getImgPath()))
                        .build())
                .toList();
        return ChapterAdminDTO.builder()
                .chapterId(ch.getChapterId())
                .chapterNumber(ch.getChapterNumber())
                .title(ch.getTitle())
                .pageCount(pages.size())
                .createdAt(ch.getCreatedAt())
                .pages(pages)
                .build();
    }

    @Override
    @Transactional
    public void createChapter(Integer comicId, ChapterAdminDTO form) {
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
    public void updateChapter(Integer chapterId, ChapterAdminDTO form) {
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

    // ── Client methods ───────────────────────────────────────────────────────

    @Override
    public ChapterDTO create(ChapterDTO request) {
        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));

        chapterRepository.findByComic_ComicIdAndChapterNumber(request.getComicId(), request.getChapterNumber())
                .ifPresent(chapter -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Chapter number already exists for this comic");
                });

        Chapters chapter = Chapters.builder()
                .comic(comic)
                .chapterNumber(request.getChapterNumber())
                .title(request.getTitle())
                .build();

        return toDTO(chapterRepository.save(chapter));
    }

    @Override
    @Transactional(readOnly = true)
    public ChapterDTO getById(Integer chapterId) {
        return toDTO(findChapterOrThrow(chapterId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterDTO> getAll() {
        return chapterRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChapterDTO> getByComicId(Integer comicId) {
        return chapterRepository.findByComic_ComicId(comicId).stream().map(this::toDTO).toList();
    }

    @Override
    public ChapterDTO update(Integer chapterId, ChapterDTO request) {
        Chapters chapter = findChapterOrThrow(chapterId);

        Comics comic = comicRepository.findById(request.getComicId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comic not found"));

        chapterRepository.findByComic_ComicIdAndChapterNumber(request.getComicId(), request.getChapterNumber())
                .filter(existing -> !existing.getChapterId().equals(chapterId))
                .ifPresent(existing -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Chapter number already exists for this comic");
                });

        chapter.setComic(comic);
        chapter.setChapterNumber(request.getChapterNumber());
        chapter.setTitle(request.getTitle());
        return toDTO(chapterRepository.save(chapter));
    }

    @Override
    public void delete(Integer chapterId) {
        Chapters chapter = findChapterOrThrow(chapterId);
        chapterRepository.delete(chapter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PageDTO> getAllPageByChapterId(Integer chapterId) {
        findChapterOrThrow(chapterId);
        return pageRepository.findByChapter_ChapterId(chapterId)
                .stream()
                .map(p -> PageDTO.builder()
                        .pageId(p.getPageId())
                        .chapterId(p.getChapter() != null ? p.getChapter().getChapterId() : null)
                        .pageNumber(p.getPageNumber())
                        .imgPath(p.getImgPath())
                        .build())
                .toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Chapters findChapterOrThrow(Integer chapterId) {
        return chapterRepository.findById(chapterId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chapter not found"));
    }

    private ChapterDTO toDTO(Chapters chapter) {
        return ChapterDTO.builder()
                .chapterId(chapter.getChapterId())
                .comicId(chapter.getComic() != null ? chapter.getComic().getComicId() : null)
                .chapterNumber(chapter.getChapterNumber())
                .title(chapter.getTitle())
                .createdAt(chapter.getCreatedAt())
                .pages(null)
                .build();
    }
}

