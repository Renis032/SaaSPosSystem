package com.renko.payload.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShiftReportDto
{
    private Long id;

    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;

    private Double totalSales;
    private Double totalRefunds;
    private Double netSales;
    private double totalOrders;

    private Long cashierId;
    private Long storeId;

    private List<PaymentSummaryDto> paymentSummaries;
    private List<Long> topSellingProductIds;
    private List<Long> recentOrderIds;
    private List<Long> refundIds;
}
