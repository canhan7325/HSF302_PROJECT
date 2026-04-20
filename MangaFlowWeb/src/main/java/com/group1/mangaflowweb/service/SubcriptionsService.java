package com.group1.mangaflowweb.service;

import com.group1.mangaflowweb.dto.SubscriptionsDTO;

import java.util.List;

public interface SubcriptionsService {
    List<SubscriptionsDTO> getAllActiveSubscriptions();
    SubscriptionsDTO getSubscriptionById(Integer id);
}
