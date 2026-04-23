package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.subscription.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.subscription.SubscriptionAdminDTO;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.repository.SubscriptionsRepository;
import com.group1.mangaflowweb.service.SubscriptionsService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SubscriptionsServiceImpl implements SubscriptionsService {

    @Autowired
    private SubscriptionsRepository subscriptionsRepository;

    // --- CLIENTS ---

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

    // --- ADMIN ---

    @Override
    public List<SubscriptionAdminDTO> getAllSubscriptionsWithCount() {
        return subscriptionsRepository.findAll().stream()
                .map(s -> SubscriptionAdminDTO.builder()
                        .subscriptionId(s.getSubscriptionId())
                        .name(s.getName())
                        .description(s.getDescription())
                        .price(s.getPrice())
                        .durationDays(s.getDurationDays())
                        .isActive(s.getIsActive())
                        .subscriberCount(s.getTransactions() != null ? s.getTransactions().size() : 0L)
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public void createSubscription(SubscriptionAdminDTO form) {
        Subscriptions subscription = new Subscriptions();
        subscription.setName(form.getName());
        subscription.setDescription(form.getDescription());
        subscription.setPrice(form.getPrice());
        subscription.setDurationDays(form.getDurationDays());
        subscription.setIsActive(true);
        subscriptionsRepository.save(subscription);
    }

    @Override
    @Transactional
    public void updateSubscription(Integer id, SubscriptionAdminDTO form) {
        Subscriptions subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setName(form.getName());
        subscription.setDescription(form.getDescription());
        subscription.setPrice(form.getPrice());
        subscription.setDurationDays(form.getDurationDays());
        subscriptionsRepository.save(subscription);
    }

    @Override
    @Transactional
    public void softDeleteSubscription(Integer id) {
        Subscriptions subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setIsActive(false);
        subscriptionsRepository.save(subscription);
    }

    @Override
    @Transactional
    public void restoreSubscription(Integer id) {
        Subscriptions subscription = subscriptionsRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setIsActive(true);
        subscriptionsRepository.save(subscription);
    }
}
