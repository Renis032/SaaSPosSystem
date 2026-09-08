package com.renko.service.impl;

import com.renko.domain.StoreStatus;
import com.renko.domain.SubscriptionPlan;
import com.renko.domain.SubscriptionStatus;
import com.renko.entities.StoreEntity;
import com.renko.entities.StoreSubscriptionEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.payload.dto.SubscriptionDto;
import com.renko.repository.StoreRepository;
import com.renko.repository.StoreSubscriptionRepository;
import com.renko.service.StoreAccessService;
import com.renko.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService
{
    private final StoreSubscriptionRepository subscriptionRepository;
    private final StoreRepository storeRepository;
    private final StoreAccessService storeAccessService;

    @Override
    public SubscriptionDto getByStoreId(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        StoreSubscriptionEntity subscription = subscriptionRepository.findByStoreEntity_Id(storeId)
                .orElseThrow(() -> ExceptionMessages.notFound("Subscription", storeId, "load store subscription"));
        return SubscriptionDto.from(subscription);
    }

    @Override
    @Transactional
    public SubscriptionDto activateTrial(Long storeId, SubscriptionPlan plan) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> ExceptionMessages.notFound("Store", storeId, "activate subscription"));
        return createTrialForNewStore(store, plan);
    }

    @Override
    @Transactional
    public SubscriptionDto createTrialForNewStore(StoreEntity store, SubscriptionPlan plan)
    {
        SubscriptionPlan resolvedPlan = plan != null ? plan : SubscriptionPlan.STARTER;
        StoreSubscriptionEntity subscription = subscriptionRepository.findByStoreEntity_Id(store.getId())
                .orElseGet(() -> StoreSubscriptionEntity.builder()
                        .storeEntity(store)
                        .build());

        subscription.setPlan(resolvedPlan);
        subscription.setStatus(SubscriptionStatus.TRIALING);
        subscription.setTrialEndsAt(LocalDateTime.now().plusDays(14));
        subscription.setCurrentPeriodEnd(subscription.getTrialEndsAt());
        if(subscription.getStripeCustomerId() == null)
        {
            subscription.setStripeCustomerId("trial_" + store.getId());
        }

        store.setStatus(StoreStatus.ACTIVE);
        storeRepository.save(store);

        return SubscriptionDto.from(subscriptionRepository.save(subscription));
    }

    @Override
    @Transactional
    public SubscriptionDto markActive(Long storeId, String stripeCustomerId, String stripeSubscriptionId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return activateFromStripeWebhook(storeId, stripeCustomerId, stripeSubscriptionId);
    }

    @Override
    @Transactional
    public SubscriptionDto activateFromStripeWebhook(Long storeId, String stripeCustomerId, String stripeSubscriptionId)
    {
        StoreEntity store = storeRepository.findById(storeId)
                .orElseThrow(() -> ExceptionMessages.notFound("Store", storeId, "mark subscription active"));

        StoreSubscriptionEntity subscription = subscriptionRepository.findByStoreEntity_Id(storeId)
                .orElseGet(() -> StoreSubscriptionEntity.builder()
                        .storeEntity(store)
                        .plan(SubscriptionPlan.STARTER)
                        .build());

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setStripeCustomerId(stripeCustomerId);
        subscription.setStripeSubscriptionId(stripeSubscriptionId);
        subscription.setCurrentPeriodEnd(LocalDateTime.now().plusMonths(1));

        store.setStatus(StoreStatus.ACTIVE);
        storeRepository.save(store);

        return SubscriptionDto.from(subscriptionRepository.save(subscription));
    }

    @Override
    public void requireActiveSubscription(Long storeId) throws Exception
    {
        StoreSubscriptionEntity subscription = subscriptionRepository.findByStoreEntity_Id(storeId)
                .orElseThrow(() -> UserException.withDetails(
                        "Store has no subscription. Activate a trial or paid plan first.",
                        ExceptionMessages.ctx("storeId", storeId)
                ));

        if(false == subscription.isEntitled())
        {
            throw UserException.withDetails(
                    "Store subscription is not active",
                    ExceptionMessages.ctx(
                            "storeId", storeId,
                            "status", subscription.getStatus(),
                            "plan", subscription.getPlan()
                    )
            );
        }

        StoreEntity store = subscription.getStoreEntity();
        if(store != null && store.getStatus() == StoreStatus.BLOCKED)
        {
            throw UserException.withDetails(
                    "Store is blocked and cannot process POS operations",
                    ExceptionMessages.ctx("storeId", storeId, "storeStatus", store.getStatus())
            );
        }
    }
}
