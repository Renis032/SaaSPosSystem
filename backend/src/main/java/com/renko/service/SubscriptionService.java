package com.renko.service;

import com.renko.domain.SubscriptionPlan;
import com.renko.entities.StoreEntity;
import com.renko.payload.dto.SubscriptionDto;

public interface SubscriptionService
{
    SubscriptionDto getByStoreId(Long storeId) throws Exception;

    SubscriptionDto activateTrial(Long storeId, SubscriptionPlan plan) throws Exception;

    SubscriptionDto createTrialForNewStore(StoreEntity store, SubscriptionPlan plan);

    SubscriptionDto markActive(Long storeId, String stripeCustomerId, String stripeSubscriptionId) throws Exception;

    SubscriptionDto activateFromStripeWebhook(Long storeId, String stripeCustomerId, String stripeSubscriptionId);

    void requireActiveSubscription(Long storeId) throws Exception;
}
