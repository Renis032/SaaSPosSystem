package com.renko.service;

import com.stripe.exception.StripeException;

public interface BillingService
{
    String createPaymentIntent(long amountCents) throws IllegalAccessException, StripeException;
    boolean verifyPayment(String paymentIntentId) throws IllegalAccessException, StripeException;
    void refundCardPayment(String paymentIntentId, long amountCents, String reason) throws Exception;
}
