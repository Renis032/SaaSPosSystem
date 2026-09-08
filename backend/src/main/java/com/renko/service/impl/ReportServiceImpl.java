package com.renko.service.impl;

import com.renko.payload.dto.StoreReportDto;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.repository.ShiftReportRepository;
import com.renko.service.ReportService;
import com.renko.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService
{
    private final StoreAccessService storeAccessService;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final InventoryRepository inventoryRepository;
    private final ShiftReportRepository shiftReportRepository;

    @Override
    @Transactional(readOnly = true)
    public StoreReportDto getStoreReport(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);

        long orderCount = orderRepository.countByStoreEntity_Id(storeId);
        Double gross = orderRepository.sumTotalAmountByStoreId(storeId);
        double grossSales = gross != null ? gross : 0.0;

        var refunds = refundRepository.findByStoreEntity_Id(storeId);
        double refundTotal = refunds.stream()
                .mapToDouble(r -> r.getAmount() != null ? r.getAmount() : 0.0)
                .sum();

        long lowStock = inventoryRepository.findLowStockByStore_Id(storeId).size();
        long openShifts = shiftReportRepository.findByStoreEntity_Id(storeId).stream()
                .filter(s -> s.getShiftEnd() == null)
                .count();

        return StoreReportDto.builder()
                .storeId(storeId)
                .orderCount(orderCount)
                .grossSales(grossSales)
                .refundTotal(refundTotal)
                .netSales(grossSales - refundTotal)
                .refundCount(refunds.size())
                .lowStockCount(lowStock)
                .openShiftCount(openShifts)
                .build();
    }
}
