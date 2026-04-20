package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.entity.Bookmarks;
import com.group1.mangaflowweb.entity.Comics;
import com.group1.mangaflowweb.entity.Users;
import com.group1.mangaflowweb.repository.BookmarkRepository;
import com.group1.mangaflowweb.repository.ComicRepository;
import com.group1.mangaflowweb.repository.UserRepository;
import com.group1.mangaflowweb.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookmarkServiceImpl implements BookmarkService {

	private final BookmarkRepository bookmarkRepository;
	private final ComicRepository comicRepository;
	private final UserRepository userRepository;

	@Override
	public boolean isBookmarked(Integer userId, Integer comicId) {
		return bookmarkRepository.existsByUser_UserIdAndComic_ComicId(userId, comicId);
	}

	@Override
	public long countFollowers(Integer comicId) {
		return bookmarkRepository.countByComic_ComicId(comicId);
	}

	@Override
	@Transactional
	public boolean toggleBookmark(Integer userId, Integer comicId) {
		return bookmarkRepository.findByUser_UserIdAndComic_ComicId(userId, comicId)
				.map(existing -> {
					bookmarkRepository.delete(existing);
					return false;
				})
				.orElseGet(() -> {
					Users user = userRepository.findById(userId)
							.orElseThrow(() -> new IllegalArgumentException("User not found"));
					Comics comic = comicRepository.findById(comicId)
							.orElseThrow(() -> new IllegalArgumentException("Comic not found"));

					Bookmarks bookmark = Bookmarks.builder()
							.user(user)
							.comic(comic)
							.build();
					bookmarkRepository.save(bookmark);
					return true;
				});
	}
}
