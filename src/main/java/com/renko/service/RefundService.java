package com.renko.service;

import com.renko.exceptions.UserException;
import com.renko.payload.dto.RefundDto;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.util.List;

public interface RefundService
{
    RefundDto createRefund(RefundDto refundDto) throws Exception;

    List<RefundDto> getAllRefunds();

    List<RefundDto> getRefundsByCashier(Long cashierId);

    List<RefundDto> getRefundByShiftReport(Long shiftReportId);

    List<RefundDto> getRefundByCashierAndDateRange(Long cashierId,
                                                   LocalDateTime start,
                                                   LocalDateTime end);

    List<RefundDto> getRefundsByStore(Long storeId);

    RefundDto getRefundById(Long refundId) throws Exception;

    void deleteRefund(Long refundId);
}
