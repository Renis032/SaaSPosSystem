package com.renko.payload.dto;

import com.renko.domain.PaymentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RefundDto
{
    private Long id;

    private OrderDto order;
    private Long orderId;

    private String reason;

    private Double amount;

    private Long shiftReportId;

    private UserDto cashierDto;
    private String cashierName;

    private Long storeId;

    private PaymentType paymentType;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
