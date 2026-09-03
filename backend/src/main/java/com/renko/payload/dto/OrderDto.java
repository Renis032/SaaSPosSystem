package com.renko.payload.dto;

import com.renko.domain.PaymentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto
{
    private Long id;

    private Double totalAmount;
    private Double subtotal;
    private Double totalDiscount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long storeId;
    private Long customerId;
    private Long cashierId;

    private String customerName;
    private String customerPhone;

    // Since we have the customer, the name phone are not necessary?
//    @com.fasterxml.jackson.annotation.JsonIgnoreProperties("store")
//    private CustomerEntity customer;

    private PaymentType paymentType;

    //When paymentType is CARD, set after Stripe confirms payment (for saving and refunds).
    private String stripePaymentIntentId;

    private List<OrderItemDto> items;
}
