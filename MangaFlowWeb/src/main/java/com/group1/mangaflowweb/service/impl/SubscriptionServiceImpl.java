package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.request.admin.SubscriptionAdRequest;
import com.group1.mangaflowweb.dto.response.admin.SubscriptionAdminResponse;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.repository.SubscriptionRepository;
import com.group1.mangaflowweb.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public List<SubscriptionAdminResponse> getAllSubscriptionsWithCount() {
        return subscriptionRepository.findAll().stream()
                .map(s -> new SubscriptionAdminResponse(
                        s.getSubscriptionId(),
                        s.getName(),
                        s.getDescription(),
                        s.getPrice(),
                        s.getDurationDays(),
                        s.getIsActive(),
                        s.getTransactions() != null ? s.getTransactions().size() : 0L))
                .toList();
    }

    @Override
    @Transactional
    public void createSubscription(SubscriptionAdRequest form) {
        Subscriptions subscription = new Subscriptions();
        subscription.setName(form.getName());
        subscription.setDescription(form.getDescription());
        subscription.setPrice(form.getPrice());
        subscription.setDurationDays(form.getDurationDays());
        subscription.setIsActive(true);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void updateSubscription(Integer id, SubscriptionAdRequest form) {
        Subscriptions subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setName(form.getName());
        subscription.setDescription(form.getDescription());
        subscription.setPrice(form.getPrice());
        subscription.setDurationDays(form.getDurationDays());
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void softDeleteSubscription(Integer id) {
        Subscriptions subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void restoreSubscription(Integer id) {
        Subscriptions subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + id));
        subscription.setIsActive(true);
        subscriptionRepository.save(subscription);
    }
}
