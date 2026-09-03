package com.renko.payload.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ReceiptDto
{
    private Long orderId;
    private String receiptNumber;
    private LocalDateTime orderDate;

    // Store Information
    private String storeName;
    private String storeAddress;
    private String storePhone;

    // Cashier Information
    private String cashierName;

    // Customer Information (optional)
    private String customerName;
    private String customerPhone;

    // Order Items
    private List<ReceiptItemDto> items;
}
