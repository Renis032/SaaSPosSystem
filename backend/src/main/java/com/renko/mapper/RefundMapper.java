package com.renko.mapper;

import com.renko.entities.RefundEntity;
import com.renko.payload.dto.RefundDto;

public class RefundMapper
{
    public static RefundDto toDto(RefundEntity refundEntity)
    {
        return RefundDto.builder()
                .id(refundEntity.getId())
                .orderId(refundEntity.getOrder() != null ? refundEntity.getOrder().getId() : null)
                .reason(refundEntity.getReason())
                .amount(refundEntity.getAmount())
                .shiftReportId(refundEntity.getShiftReportEntity() != null
                        ? refundEntity.getShiftReportEntity().getId()
                        : null)
                .cashierId(refundEntity.getCashierEntity() != null
                        ? refundEntity.getCashierEntity().getId()
                        : null)
                .cashierName(refundEntity.getCashierEntity() != null
                        ? refundEntity.getCashierEntity().getFullName()
                        : null)
                .storeId(refundEntity.getStoreEntity() != null ? refundEntity.getStoreEntity().getId() : null)
                .paymentType(refundEntity.getPaymentType())
                .createdAt(refundEntity.getCreatedAt())
                .updatedAt(refundEntity.getUpdatedAt())
                .build();
    }
}
