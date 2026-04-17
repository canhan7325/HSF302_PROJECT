package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Transactions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionsRepository extends JpaRepository<Transactions, Integer> {
}