package com.renko.service.impl;

import com.renko.domain.PaymentType;
import com.renko.entities.*;
import com.renko.exceptions.UserException;
import com.renko.mapper.ShiftReportMapper;
import com.renko.payload.dto.ShiftReportDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.repository.ShiftReportRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.ShiftReportService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftReportServiceImpl implements ShiftReportService
{
    private final ShiftReportRepository shiftReportRepository;
    private final OrderRepository orderRepository;
    private final RefundRepository refundRepository;
    private final StoreRepository storeRepository;
    private final UserService userService;
    private final UserRepository userRepository;

@Override
    public ShiftReportDto startShift() throws Exception
    {
        UserDto currentUser = userService.getCurrentUser();
        LocalDateTime shiftStart = LocalDateTime.now();

        Optional<ShiftReportEntity> existing = shiftReportRepository
                .findTopByCashierEntity_IdAndShiftEndIsNullOrderByShiftStartDesc(currentUser.getId());

        if(existing.isPresent())
        {
            throw new Exception("Cashier already has an active shift. userId=" + currentUser.getId()
                    + ", email=" + currentUser.getEmail()
                    + ", activeShiftId=" + existing.get().getId()
                    + ", shiftStart=" + existing.get().getShiftStart());
        }

        if(currentUser.getStoreId() == null)
        {
            throw new Exception("Cashier has no storeId; cannot start shift. userId="
                    + currentUser.getId() + ", email=" + currentUser.getEmail());
        }

        UserEntity cashierEntity = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new Exception("Cashier not found with id: " + currentUser.getId()));
        StoreEntity store = storeRepository.findById(currentUser.getStoreId())
                .orElseThrow(() -> new Exception("Store not found with id: " + currentUser.getStoreId()
                        + "; cannot start shift for cashierId=" + currentUser.getId()));

        ShiftReportEntity shiftReport = ShiftReportEntity.builder()
                .shiftStart(shiftStart)
                .cashierEntity(cashierEntity)
                .storeEntity(store)
                .topSellingProducts(new ArrayList<>())
                .recentOrders(new ArrayList<>())
                .refunds(new ArrayList<>())
                .build();

        ShiftReportEntity savedReport = shiftReportRepository.save(shiftReport);
        return ShiftReportMapper.toDto(savedReport);
    }

    @Override
    public ShiftReportDto endShift(Long shiftReportId, LocalDateTime shiftEnd) throws Exception
    {
        UserDto cashier =  userService.getCurrentUser();

        ShiftReportEntity shiftReport = shiftReportRepository
                .findTopByCashierEntity_IdAndShiftEndIsNullOrderByShiftStartDesc(cashier.getId())
                .orElseThrow(() -> new Exception("No active shift found for cashierId=" + cashier.getId()
                        + ", email=" + cashier.getEmail()
                        + (shiftReportId != null ? ", requestedShiftReportId=" + shiftReportId : "")));

        shiftReport.setShiftEnd(shiftEnd);

        List<RefundEntity> refunds = refundRepository.findByCashierEntity_IdAndCreatedAtBetween(cashier.getId(),
                                                                                                shiftReport.getShiftStart(),
                                                                                                shiftReport.getShiftEnd());

        double totalRefunds = refunds.stream()
                .mapToDouble(refund -> refund.getAmount() != null ? refund.getAmount() : 0.0).sum();

        List<OrderEntity> orders = orderRepository.findByCashierEntity_IdAndCreatedAtBetween(cashier.getId(),
                shiftReport.getShiftStart(),
                shiftReport.getShiftEnd());

        double totalSales = orders.stream()
                .mapToDouble(OrderEntity::getTotalAmount).sum();

        int totalOrders = orders.size();

        double netSales = totalSales - totalRefunds;

        shiftReport.setTotalRefunds(totalRefunds);
        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalOrders(totalOrders);
        shiftReport.setNetSales(netSales);
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setRefunds(refunds);

        ShiftReportEntity savedReport = shiftReportRepository.save(shiftReport);
        return ShiftReportMapper.toDto(savedReport);
    }

    @Override
    public ShiftReportDto getShiftReportById(Long id) throws Exception
    {
        return shiftReportRepository.findById(id)
                .map(ShiftReportMapper::toDto)
                .orElseThrow(() -> new Exception("Shift report not found with id: " + id));
    }

    @Override
    public List<ShiftReportDto> getAllShiftReports()
    {
        return shiftReportRepository.findAll().stream()
                .map(ShiftReportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDto> getShiftReportsByStoreId(Long storeId)
    {
        return shiftReportRepository.findByStoreEntity_Id(storeId).stream()
                .map(ShiftReportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShiftReportDto> getShiftReportsByCashierEntity_Id(Long cashierId)
    {
        return shiftReportRepository.findByCashierEntity_Id(cashierId).stream()
                .map(ShiftReportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ShiftReportDto getCurrentShiftProgress(Long cashierId) throws UserException
    {
        UserDto cashier = userService.getCurrentUser();

        ShiftReportEntity shiftReport = shiftReportRepository
                .findTopByCashierEntity_IdAndShiftEndIsNullOrderByShiftStartDesc(cashier.getId())
                .orElse(null);

        if(shiftReport == null)
        {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();

        List<OrderEntity> orders = orderRepository.findByCashierEntity_IdAndCreatedAtBetween(cashier.getId(), shiftReport.getShiftStart(), now);

        List<RefundEntity> refunds = refundRepository.findByCashierEntity_IdAndCreatedAtBetween(cashier.getId(), shiftReport.getShiftStart(), now);

        double totalRefunds = refunds.stream()
                .mapToDouble(refund -> refund.getAmount() != null ? refund.getAmount() : 0.0).sum();

        double totalSales = orders.stream()
                .mapToDouble(OrderEntity::getTotalAmount).sum();

        int totalOrders = orders.size();

        double netSales = totalSales - totalRefunds;

        shiftReport.setTotalOrders(totalRefunds);
        shiftReport.setTotalSales(totalSales);
        shiftReport.setTotalOrders(totalOrders);
        shiftReport.setNetSales(netSales);
        shiftReport.setRecentOrders(getRecentOrders(orders));
        shiftReport.setTopSellingProducts(getTopSellingProducts(orders));
        shiftReport.setPaymentSummaries(getPaymentSummaries(orders, totalSales));
        shiftReport.setRefunds(refunds);

        ShiftReportEntity saved = shiftReportRepository.save(shiftReport);
        return ShiftReportMapper.toDto(saved);
    }

    @Override
    public ShiftReportDto getShiftByCashierEntityAndDate(Long cashierId, LocalDateTime date) throws Exception
    {
        UserEntity cashier = userRepository.findById(cashierId)
                .orElseThrow(() -> new Exception("Cashier not found with id: " + cashierId
                        + "; cannot lookup shift for date=" + date));

        LocalDateTime start = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = date.withHour(23).withMinute(59).withSecond(59);

        ShiftReportEntity report = shiftReportRepository.findByCashierEntityAndShiftStartBetween(cashier, start, end)
                .orElseThrow(() -> new Exception("No shift found for cashierId=" + cashierId
                        + " between " + start + " and " + end));

        return ShiftReportMapper.toDto(report);
    }

    private List<PaymentSummaryEntity> getPaymentSummaries(List<OrderEntity> orders, double totalSales)
    {
        Map<PaymentType, List<OrderEntity>> grouped = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getPaymentType() != null ? order.getPaymentType() : PaymentType.CASH));

        List<PaymentSummaryEntity> summaries = new ArrayList<>();

        for(Map.Entry<PaymentType, List<OrderEntity>> entry : grouped.entrySet())
        {
            double amount = entry.getValue().stream()
                    .mapToDouble(OrderEntity::getTotalAmount)
                    .sum();

            int transactionsCount = entry.getValue().size();
            double percent = (amount / totalSales) * 100;

            PaymentSummaryEntity ps = new PaymentSummaryEntity();
            ps.setType(entry.getKey());
            ps.setTotalAmount(amount);
            ps.setTransactionCount(transactionsCount);
            ps.setPercentage(percent);

            summaries.add(ps);
        }

        return summaries;
    }

    private List<ProductEntity> getTopSellingProducts(List<OrderEntity> orders)
    {
        Map<ProductEntity, Integer> productSalesMap = new HashMap<>();

        for(var order : orders)
        {
            for(var item : order.getItems())
            {
                ProductEntity product = item.getProductEntity();
                productSalesMap.put(product, productSalesMap.getOrDefault(product, 0) + item.getQuantity());
            }
        }

        return  productSalesMap.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

    }

    private List<OrderEntity> getRecentOrders(List<OrderEntity> orders)
    {
        return orders.stream()
                .sorted(Comparator.comparing(OrderEntity::getCreatedAt).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }
}
