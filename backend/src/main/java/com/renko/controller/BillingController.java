package com.renko.controller;

import com.renko.service.BillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController
{
    private final BillingService billingService;

    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Long> body)
    {
        Long amountCents = body != null ? body.get("amountCents") : null;
        if(null == amountCents || amountCents <= 0)
        {
            return ResponseEntity.badRequest().build();
        }

        try
        {
            String clientSecret = billingService.createPaymentIntent(amountCents);
            return ResponseEntity.ok(Map.of("clientSecret", clientSecret));
        }
        catch(Exception e)
        {
            return ResponseEntity.internalServerError().body(Map.of("Error", e.getMessage() != null ? e.getMessage() : "Failed to create payment intent"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> body)
    {
        String paymentIntentId = body != null ? body.get("paymentIntentId") : null;
        if(paymentIntentId == null || paymentIntentId.isBlank())
        {
            return ResponseEntity.badRequest().body(Map.of("error", "paymentIntentId is required"));
        }

        try
        {
            boolean verified = billingService.verifyPayment(paymentIntentId);
            return ResponseEntity.ok(Map.of("paymentIntentId", paymentIntentId, "verified", verified));
        }
        catch(Exception e)
        {
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage() != null ? e.getMessage() : "Payment verification failed"
            ));
        }
    }

    @PostMapping("/refund")
    public ResponseEntity<Map<String, String>> refund(@RequestBody Map<String, Object> body)
    {
        String paymentIntentId = body != null && body.get("paymentIntentId") != null ? body.get("paymentIntentId").toString() : null;
        Number amount = body != null && body.get("amountCents") != null ? (Number) body.get("amountCents") : null;
        String reason = body != null && body.get("reason") != null ? body.get("reason").toString() : null;

        if(paymentIntentId == null || paymentIntentId.isBlank() || amount == null || amount.longValue() <= 0)
        {
            return ResponseEntity.badRequest().body(Map.of("error", "paymentIntentId and amountCents are required"));
        }

        long amountCents = amount.longValue();
        try
        {
            billingService.refundCardPayment(paymentIntentId, amountCents, reason);
            return ResponseEntity.ok(Map.of("message", "Refund initiated successfully"));
        }
        catch(Exception e)
        {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Refund failed"));
        }
    }
}
