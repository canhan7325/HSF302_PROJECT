package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.request.admin.SubscriptionAdRequest;
import com.group1.mangaflowweb.dto.response.admin.SubscriptionAdminResponse;

import java.util.List;

public interface SubscriptionService {

    List<SubscriptionAdminResponse> getAllSubscriptionsWithCount();

    void createSubscription(SubscriptionAdRequest form);

    void updateSubscription(Integer id, SubscriptionAdRequest form);

    void softDeleteSubscription(Integer id);

    void restoreSubscription(Integer id);
}
