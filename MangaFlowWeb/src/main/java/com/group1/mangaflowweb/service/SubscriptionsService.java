package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.subscription.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.subscription.SubscriptionAdminDTO;

import java.util.List;

public interface SubscriptionsService {

    // --- CLIENTS ---
    List<SubscriptionsDTO> getAllActiveSubscriptions();
    SubscriptionsDTO getSubscriptionById(Integer id);

    // --- ADMIN ---
    List<SubscriptionAdminDTO> getAllSubscriptionsWithCount();
    void createSubscription(SubscriptionAdminDTO form);
    void updateSubscription(Integer id, SubscriptionAdminDTO form);
    void softDeleteSubscription(Integer id);
    void restoreSubscription(Integer id);
}

