package com.renko.mapper;

import com.renko.entities.RefundEntity;
import com.renko.payload.dto.RefundDto;

public class RefundMapper
{
    public static RefundDto toDto(RefundEntity refundEntity)
    {
        return RefundDto.builder()
                .id(refundEntity.getId())
                .order(OrderMapper.toDto(refundEntity.getOrder()))
                .orderId(refundEntity.getOrder().getId())
                .reason(refundEntity.getReason())
                .amount(refundEntity.getAmount())
                .shiftReportId(refundEntity.getShiftReportEntity().getId())
                .cashierDto(UserMapper.toDto(refundEntity.getCashierEntity()))
                .cashierName(refundEntity.getCashierEntity().getFullName())
                .storeId(refundEntity.getStoreEntity().getId())
                .paymentType(refundEntity.getPaymentType())
                .createdAt(refundEntity.getCreatedAt())
                .updatedAt(refundEntity.getUpdatedAt())
                .build();
    }
}
