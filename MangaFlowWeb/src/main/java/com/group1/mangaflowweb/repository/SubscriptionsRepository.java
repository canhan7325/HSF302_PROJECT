package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscriptions, Integer> {
    List<Subscriptions> findAllByIsActiveTrue();
    List<Subscriptions> findByIsActiveTrue();
}