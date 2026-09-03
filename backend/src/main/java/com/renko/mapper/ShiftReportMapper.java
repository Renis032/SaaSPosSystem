package com.renko.mapper;

import com.renko.entities.OrderEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.RefundEntity;
import com.renko.entities.ShiftReportEntity;
import com.renko.payload.dto.OrderDto;
import com.renko.payload.dto.ProductDto;
import com.renko.payload.dto.RefundDto;
import com.renko.payload.dto.ShiftReportDto;

import java.util.List;
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
                .cashierId(entity.getCashierEntity().getId())
                .storeId(entity.getStoreEntity().getId())
                .paymentSummaries(entity.getPaymentSummaries())
                .topSellingProducts(mapProductsToDto(entity.getTopSellingProducts()))
                .recentOrders(mapOrdersToDto(entity.getRecentOrders()))
                .refunds(mapRefundsToDto(entity.getRefunds()))
                .build();
    }

    private static List<ProductDto> mapProductsToDto(List<ProductEntity> entities)
    {
        return entities.stream()
                .map(ProductMapper::toDto)
                .collect(Collectors.toList());
    }

    private static List<OrderDto> mapOrdersToDto(List<OrderEntity> entities)
    {
        return entities.stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    private static List<RefundDto> mapRefundsToDto(List<RefundEntity> entities)
    {
        return entities.stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }
}
