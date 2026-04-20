package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {

    @Query("SELECT t FROM Transactions t " +
           "JOIN FETCH t.subscription s " +
           "WHERE t.user.userId = :userId " +
           "ORDER BY t.createdAt DESC")
    List<Transactions> findByUserIdOrderByCreatedAtDesc(@Param("userId") Integer userId);
}