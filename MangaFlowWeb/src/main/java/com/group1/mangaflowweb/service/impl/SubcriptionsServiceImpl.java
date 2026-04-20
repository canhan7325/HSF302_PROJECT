package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.SubscriptionsDTO;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.repository.SubscriptionsRepository;
import com.group1.mangaflowweb.service.SubcriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubcriptionsServiceImpl implements SubcriptionsService {

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    @Override
    public List<SubscriptionsDTO> getAllActiveSubscriptions() {
        return subscriptionsRepository.findAllByIsActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionsDTO getSubscriptionById(Integer id) {
        Optional<Subscriptions> subscription = subscriptionsRepository.findById(id);
        return subscription.map(this::convertToDTO).orElse(null);
    }

    private SubscriptionsDTO convertToDTO(Subscriptions subscription) {
        return SubscriptionsDTO.builder()
                .subscriptionId(subscription.getSubscriptionId())
                .name(subscription.getName())
                .description(subscription.getDescription())
                .price(subscription.getPrice())
                .durationDays(subscription.getDurationDays())
                .isActive(subscription.getIsActive())
                .build();
    }
}
