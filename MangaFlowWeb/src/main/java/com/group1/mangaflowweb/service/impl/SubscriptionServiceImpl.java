package com.group1.mangaflowweb.service.impl;

import com.group1.mangaflowweb.dto.SubscriptionsDTO;
import com.group1.mangaflowweb.dto.request.admin.SubscriptionAdDTO;
import com.group1.mangaflowweb.dto.response.admin.SubscriptionAdminResponse;
import com.group1.mangaflowweb.entity.Subscriptions;
import com.group1.mangaflowweb.repository.SubscriptionRepository;
import com.group1.mangaflowweb.service.SubscriptionService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    // ── Admin CRUD ────────────────────────────────────────────────────────────

    @Override
    public List<SubscriptionAdminResponse> getAllSubscriptionsWithCount() {
        return subscriptionRepository.findAll().stream()
                .map(s -> new SubscriptionAdminResponse(
                        s.getSubscriptionId(), s.getName(), s.getDescription(),
                        s.getPrice(), s.getDurationDays(), s.getIsActive(),
                        s.getTransactions() != null ? s.getTransactions().size() : 0L))
                .toList();
    }

    @Override
    @Transactional
    public void createSubscription(SubscriptionAdDTO form) {
        Subscriptions s = new Subscriptions();
        s.setName(form.getName());
        s.setDescription(form.getDescription());
        s.setPrice(form.getPrice());
        s.setDurationDays(form.getDurationDays());
        s.setIsActive(true);
        subscriptionRepository.save(s);
    }

    @Override
    @Transactional
    public void updateSubscription(Integer id, SubscriptionAdDTO form) {
        Subscriptions s = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + id));
        s.setName(form.getName());
        s.setDescription(form.getDescription());
        s.setPrice(form.getPrice());
        s.setDurationDays(form.getDurationDays());
        subscriptionRepository.save(s);
    }

    @Override
    @Transactional
    public void softDeleteSubscription(Integer id) {
        Subscriptions s = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + id));
        s.setIsActive(false);
        subscriptionRepository.save(s);
    }

    @Override
    @Transactional
    public void restoreSubscription(Integer id) {
        Subscriptions s = subscriptionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found: " + id));
        s.setIsActive(true);
        subscriptionRepository.save(s);
    }

    // ── Pricing / payment flow ────────────────────────────────────────────────

    @Override
    public List<SubscriptionsDTO> getAllActiveSubscriptions() {
        return subscriptionRepository.findByIsActiveTrue().stream()
                .filter(s -> s.getPrice() != null
                        && s.getPrice().compareTo(BigDecimal.valueOf(1000)) >= 0)
                .map(this::toDTO)
                .toList();
    }

    @Override
    public SubscriptionsDTO getSubscriptionById(Integer id) {
        return subscriptionRepository.findById(id).map(this::toDTO).orElse(null);
    }

    private SubscriptionsDTO toDTO(Subscriptions s) {
        return SubscriptionsDTO.builder()
                .subscriptionId(s.getSubscriptionId())
                .name(s.getName())
                .description(s.getDescription())
                .price(s.getPrice())
                .durationDays(s.getDurationDays())
                .isActive(s.getIsActive())
                .build();
    }
}
