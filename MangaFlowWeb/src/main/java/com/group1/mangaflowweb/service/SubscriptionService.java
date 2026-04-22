package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.request.admin.SubscriptionAdDTO;
import com.group1.mangaflowweb.dto.response.admin.SubscriptionAdminResponse;

import java.util.List;

public interface SubscriptionService {

    // Admin CRUD
    List<SubscriptionAdminResponse> getAllSubscriptionsWithCount();
    void createSubscription(SubscriptionAdDTO form);
    void updateSubscription(Integer id, SubscriptionAdDTO form);
    void softDeleteSubscription(Integer id);
    void restoreSubscription(Integer id);

    // Pricing / payment flow
    List<SubscriptionsDTO> getAllActiveSubscriptions();
    SubscriptionsDTO getSubscriptionById(Integer id);
}
