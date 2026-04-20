package com.group1.mangaflowweb.repository;

import com.group1.mangaflowweb.entity.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscriptions, Integer> {
    
    // Get all active subscriptions
    List<Subscriptions> findByIsActiveTrue();
}
