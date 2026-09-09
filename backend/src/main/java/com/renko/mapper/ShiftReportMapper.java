package com.renko.mapper;

import com.renko.entities.OrderEntity;
import com.renko.entities.PaymentSummaryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.RefundEntity;
import com.renko.entities.ShiftReportEntity;
import com.renko.payload.dto.PaymentSummaryDto;
import com.renko.payload.dto.ShiftReportDto;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ShiftReportMapper
{
    public static ShiftReportDto toDto(ShiftReportEntity entity)
    {
        if(entity == null)
        {
            return null;
        }

        return ShiftReportDto.builder()
                .id(entity.getId())
                .shiftStart(entity.getShiftStart())
                .shiftEnd(entity.getShiftEnd())
                .totalSales(entity.getTotalSales())
                .totalRefunds(entity.getTotalRefunds())
                .netSales(entity.getNetSales())
                .totalOrders(entity.getTotalOrders())
                .cashierId(entity.getCashierEntity() != null ? entity.getCashierEntity().getId() : null)
                .storeId(entity.getStoreEntity() != null ? entity.getStoreEntity().getId() : null)
                .branchId(entity.getBranchEntity() != null ? entity.getBranchEntity().getId() : null)
                .paymentSummaries(mapPaymentSummaries(entity.getPaymentSummaries()))
                .topSellingProductIds(mapProductIds(entity.getTopSellingProducts()))
                .recentOrderIds(mapOrderIds(entity.getRecentOrders()))
                .refundIds(mapRefundIds(entity.getRefunds()))
                .build();
    }

    private static List<PaymentSummaryDto> mapPaymentSummaries(List<PaymentSummaryEntity> entities)
    {
        if(entities == null)
        {
            return Collections.emptyList();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(summary -> PaymentSummaryDto.builder()
                        .type(summary.getType())
                        .totalAmount(summary.getTotalAmount())
                        .transactionCount(summary.getTransactionCount())
                        .percentage(summary.getPercentage())
                        .build())
                .collect(Collectors.toList());
    }

    private static List<Long> mapProductIds(List<ProductEntity> entities)
    {
        if(entities == null)
        {
            return Collections.emptyList();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(ProductEntity::getId)
                .collect(Collectors.toList());
    }

    private static List<Long> mapOrderIds(List<OrderEntity> entities)
    {
        if(entities == null)
        {
            return Collections.emptyList();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(OrderEntity::getId)
                .collect(Collectors.toList());
    }

    private static List<Long> mapRefundIds(List<RefundEntity> entities)
    {
        if(entities == null)
        {
            return Collections.emptyList();
        }

        return entities.stream()
                .filter(Objects::nonNull)
                .map(RefundEntity::getId)
                .collect(Collectors.toList());
    }
}
