package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.SubscriptionRequest;
import com.group1.mangaflowweb.dto.response.SubscriptionAdminResponse;

import java.util.List;

public interface SubscriptionService {

    List<SubscriptionAdminResponse> getAllSubscriptionsWithCount();

    void createSubscription(SubscriptionRequest form);

    void updateSubscription(Integer id, SubscriptionRequest form);

    void softDeleteSubscription(Integer id);

    void restoreSubscription(Integer id);
}
