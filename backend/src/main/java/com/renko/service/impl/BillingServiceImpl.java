package com.renko.service.impl;

import com.renko.service.BillingService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams.Reason;
import com.stripe.param.RefundCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BillingServiceImpl implements BillingService
{
    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.currency:usd}")
    private String currency;

    @PostConstruct
    public void init()
    {
        if(stripeApiKey != null && false == stripeApiKey.isBlank())
        {
            Stripe.apiKey = stripeApiKey;
            log.info("Stripe API key configure successfully!");
        }
        else
        {
            log.warn("Stripe API key is not configures");
        }
    }

    @Override
    public String createPaymentIntent(long amountCents) throws IllegalAccessException, StripeException
    {
        ensureStripeConfigured();
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                                                                    .setAmount(amountCents)
                                                                    .setCurrency(currency)
                                                                    .setAutomaticPaymentMethods(PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                                                                                                                                 .setEnabled(true)
                                                                                                                                                 .build())
                                                                    .build();

        PaymentIntent intent = PaymentIntent.create(params);
        return intent.getClientSecret();
    }

    @Override
    public boolean verifyPayment(String paymentIntentId) throws IllegalAccessException, StripeException
    {
        if(paymentIntentId == null || paymentIntentId.isBlank())
        {
            return false;
        }

        ensureStripeConfigured();
        PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
        return intent.getStatus().equals("succeeded");
    }

    @Override
    public void refundCardPayment(String paymentIntentId, long amountCents, String reason) throws Exception
    {
        ensureStripeConfigured();

        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                                                                     .setPaymentIntent(paymentIntentId)
                                                                     .setAmount(amountCents);

        if(reason != null && false == reason.isBlank())
        {
            Reason r = "duplicate".equalsIgnoreCase(reason) ? Reason.DUPLICATE :
                       "fraudulent".equalsIgnoreCase(reason) ? Reason.FRAUDULENT : Reason.REQUESTED_BY_CUSTOMER;

            paramsBuilder.setReason(r);
        }

        try
        {
            Refund.create(paramsBuilder.build());
        }
        catch(StripeException e)
        {
            log.error("Stripe refund failed: {}", e.getMessage());
            throw new Exception("Stripe refund failed for paymentIntentId=" + paymentIntentId
                    + ", amountCents=" + amountCents
                    + ", reason=" + reason
                    + ": " + e.getMessage());
        }
    }

    private void ensureStripeConfigured() throws IllegalAccessException
    {
        if(stripeApiKey == null || stripeApiKey.isBlank())
        {
            throw new IllegalAccessException("Stripe secret key is not configured. Set stripe.api.key (or equivalent) before using billing endpoints.");
        }
    }
}
