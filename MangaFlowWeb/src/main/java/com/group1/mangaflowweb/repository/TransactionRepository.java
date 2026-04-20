package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Transactions;
import com.group1.mangaflowweb.enums.TransactionEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TransactionRepository extends JpaRepository<Transactions, Integer> {

    boolean existsByUser_UserIdAndStatusAndEndedAtAfter(Integer userId,
                                                        TransactionEnum status,
                                                        LocalDateTime endedAt);
}
