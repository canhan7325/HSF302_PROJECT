package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.response.ComicDetailResponse;
import com.group1.mangaflowweb.entity.Chapters;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.ChapterRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.BookmarkService;
import com.group1.mangaflowweb.service.ComicService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ComicServiceImpl implements ComicService {

	private static final DateTimeFormatter CHAPTER_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private final ComicRepository comicRepository;
	private final ChapterRepository chapterRepository;
	private final UserRepository userRepository;
	private final BookmarkService bookmarkService;

	@Override
	@Transactional
	public ComicDetailResponse getComicDetail(String slug, String username) {
		Comics comic = comicRepository.findBySlug(slug)
				.orElseThrow(() -> new IllegalArgumentException("Comic not found"));

		comic.setViewCount((comic.getViewCount() == null ? 0 : comic.getViewCount()) + 1);
		comicRepository.save(comic);

		List<Chapters> chapters = chapterRepository.findByComic_ComicIdOrderByChapterNumberDesc(comic.getComicId());
		long followerCount = bookmarkService.countFollowers(comic.getComicId());
		boolean bookmarked = resolveBookmarked(username, comic.getComicId());

		return ComicDetailResponse.builder()
				.comicId(comic.getComicId())
				.title(comic.getTitle())
				.slug(comic.getSlug())
				.authorName(comic.getUser() != null ? comic.getUser().getUsername() : "Unknown")
				.description(comic.getDescription())
				.coverImg(comic.getCoverImg())
				.viewCount(comic.getViewCount())
				.followerCount(followerCount)
				.bookmarked(bookmarked)
				.genres(comic.getGenreComics().stream()
						.map(gc -> gc.getGenre().getName())
						.toList())
				.chapters(chapters.stream().map(this::toChapterItem).toList())
				.build();
	}

	private boolean resolveBookmarked(String username, Integer comicId) {
		if (username == null || username.isBlank()) {
			return false;
		}

		return userRepository.findByUsername(username)
				.map(Users::getUserId)
				.map(userId -> bookmarkService.isBookmarked(userId, comicId))
				.orElse(false);
	}

	private ComicDetailResponse.ChapterItem toChapterItem(Chapters chapter) {
		return ComicDetailResponse.ChapterItem.builder()
				.chapterId(chapter.getChapterId())
				.chapterNumber(chapter.getChapterNumber())
				.title(chapter.getTitle())
				.createdAt(chapter.getCreatedAt() != null ? chapter.getCreatedAt().format(CHAPTER_DATE_FORMAT) : "-")
				.build();
	}
    @Override
    public void addComic(ComicRequest comicRequest) {
        Comics comic = Comics.builder()
                .title(comicRequest.getTitle())
                .slug(comicRequest.getSlug())
                .description(comicRequest.getDescription())
                .coverImg(comicRequest.getCoverImg())
                .user(comicRequest.getUser())
                .build();

        comic = comicRepository.save(comic);

        if (comicRequest.getChapterRequests() != null) {
            for (ChapterRequest chapterRequest : comicRequest.getChapterRequests()) {
                chapterService.addChapter(chapterRequest, comic);
            }
        }
    }
}
