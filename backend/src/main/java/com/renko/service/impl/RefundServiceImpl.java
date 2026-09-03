package com.renko.service.impl;

import com.renko.domain.PaymentType;
import com.renko.entities.*;
import com.renko.mapper.RefundMapper;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.RefundDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.service.BillingService;
import com.renko.service.RefundService;
import com.renko.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService
{
    private final RefundRepository refundRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final BillingService billingService;

    @Override
    public RefundDto createRefund(RefundDto refundDto) throws Exception
    {
        OrderEntity order = orderRepository.findById(refundDto.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Order not found with id: " + refundDto.getOrderId() + "; cannot create refund"));

        StoreEntity store = order.getStoreEntity();
        if(store == null)
        {
            throw new EntityNotFoundException("Order id=" + refundDto.getOrderId()
                    + " has no linked store; cannot create refund amount=" + refundDto.getAmount());
        }

        UserDto cashier = userService.getCurrentUser();

        if(order.getPaymentType() == PaymentType.CARD
           && order.getStripePaymentIntentId() != null
           && false == order.getStripePaymentIntentId().isBlank()
           && refundDto.getAmount() != null
           && refundDto.getAmount() > 0)
        {
            long amountCents = Math.round(refundDto.getAmount() * 100);
            if(amountCents > 0)
            {
                billingService.refundCardPayment(order.getStripePaymentIntentId(),
                                                 amountCents,
                                                 refundDto.getReason());
            }
        }

        for(var item : order.getItems())
        {
            InventoryEntity inventory = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(store.getId(),
                                                                                                    item.getProductEntity().getId());

            if(inventory != null)
            {
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);
            }
        }

        RefundEntity refund = RefundEntity.builder()
                .order(order)
                .amount(refundDto.getAmount())
                .cashierEntity(UserMapper.toEntity(cashier))
                .storeEntity(store)
                .paymentType(order.getPaymentType())
                .build();

        RefundEntity saved = refundRepository.save(refund);
        return RefundMapper.toDto(saved);
    }

    @Override
    public List<RefundDto> getAllRefunds()
    {
        return refundRepository.findAll().stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByCashier(Long cashierId)
    {
        return refundRepository.findByCashierEntity_Id(cashierId).stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByShiftReport(Long shiftReportId)
    {
        return refundRepository.findByShiftReportEntity_Id(shiftReportId).stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundByCashierAndDateRange(Long cashierId,
                                                          LocalDateTime start,
                                                          LocalDateTime end)
    {
        return refundRepository.findByCashierEntity_IdAndCreatedAtBetween(cashierId, start, end).stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<RefundDto> getRefundsByStore(Long storeId)
    {
        return refundRepository.findByStoreEntity_Id(storeId).stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RefundDto getRefundById(Long refundId) throws Exception
    {
        return refundRepository.findById(refundId)
                .map(RefundMapper::toDto)
                .orElseThrow(() -> new Exception("Refund not found with id: " + refundId));
    }

    @Override
    public void deleteRefund(Long refundId)
    {
        refundRepository.deleteById(refundId);
    }
}
