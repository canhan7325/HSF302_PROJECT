package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.entity.Users;

public interface AccessService {

    boolean canReadFullChapter(Users user);
}
