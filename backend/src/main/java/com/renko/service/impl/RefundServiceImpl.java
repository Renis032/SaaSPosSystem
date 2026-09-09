package com.renko.service.impl;

import com.renko.domain.PaymentType;
import com.renko.entities.*;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.mapper.RefundMapper;
import com.renko.payload.dto.RefundDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.RefundRepository;
import com.renko.repository.ShiftReportRepository;
import com.renko.repository.UserRepository;
import com.renko.service.AuditLogService;
import com.renko.service.BillingService;
import com.renko.service.RefundService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService
{
    private final RefundRepository refundRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final InventoryRepository inventoryRepository;
    private final BillingService billingService;
    private final ShiftReportRepository shiftReportRepository;
    private final StoreAccessService storeAccessService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public RefundDto createRefund(RefundDto refundDto) throws Exception
    {
        if(refundDto.getOrderId() == null)
        {
            throw ExceptionMessages.required(
                    "orderId",
                    "orderId is required to create a refund. Create an order first."
            );
        }

        OrderEntity order = orderRepository.findDetailedById(refundDto.getOrderId())
                .orElseGet(() -> orderRepository.findById(refundDto.getOrderId()).orElse(null));
        if(order == null)
        {
            throw ExceptionMessages.notFound(
                    "Order",
                    refundDto.getOrderId(),
                    "create refund"
            );
        }

        StoreEntity store = order.getStoreEntity();
        if(store == null)
        {
            throw ExceptionMessages.notFound(
                    "Store",
                    null,
                    "create refund for orderId=" + refundDto.getOrderId()
                            + " (order has no linked store)"
            );
        }
        storeAccessService.requireStoreAccess(store.getId());

        UserDto cashier = userService.getCurrentUser();
        UserEntity cashierEntity = userRepository.findById(cashier.getId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Cashier",
                        cashier.getId(),
                        "create refund"
                ));

        ShiftReportEntity shiftReport = null;
        if(refundDto.getShiftReportId() != null)
        {
            shiftReport = shiftReportRepository.findById(refundDto.getShiftReportId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Shift report",
                            refundDto.getShiftReportId(),
                            "create refund for orderId=" + refundDto.getOrderId()
                    ));
        }

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

        if(order.getItems() != null)
        {
            for(var item : order.getItems())
            {
                if(item.getProductEntity() == null || item.getProductEntity().getId() == null)
                {
                    throw ExceptionMessages.required(
                            "productId",
                            "Refund cannot restore inventory because an order item has no product"
                    );
                }

                InventoryEntity inventory = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(
                        store.getId(),
                        item.getProductEntity().getId()
                );

                if(inventory == null)
                {
                    throw UserException.withDetails(
                            "Cannot refund: product has no inventory row for this store",
                            ExceptionMessages.ctx(
                                    "productId", item.getProductEntity().getId(),
                                    "storeId", store.getId(),
                                    "orderId", order.getId()
                            )
                    );
                }

                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);
            }
        }

        RefundEntity refund = RefundEntity.builder()
                .order(order)
                .reason(refundDto.getReason())
                .amount(refundDto.getAmount())
                .shiftReportEntity(shiftReport)
                .cashierEntity(cashierEntity)
                .storeEntity(store)
                .paymentType(refundDto.getPaymentType() != null ? refundDto.getPaymentType() : order.getPaymentType())
                .build();

        RefundEntity saved = refundRepository.save(refund);
        auditLogService.record(
                store.getId(),
                "REFUND_CREATE",
                "Refund",
                String.valueOf(saved.getId()),
                "orderId=" + order.getId() + "; amount=" + saved.getAmount()
        );
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
    public List<RefundDto> getRefundsByStore(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return refundRepository.findByStoreEntity_Id(storeId).stream()
                .map(RefundMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public RefundDto getRefundById(Long refundId) throws Exception
    {
        return refundRepository.findById(refundId)
                .map(RefundMapper::toDto)
                .orElseThrow(() -> ExceptionMessages.notFound("Refund", refundId));
    }

    @Override
    public void deleteRefund(Long refundId)
    {
        RefundEntity refund = refundRepository.findById(refundId)
                .orElseThrow(() -> ExceptionMessages.notFound("Refund", refundId, "delete"));
        refundRepository.delete(refund);
    }

    @Override
    public void deleteAllRefunds()
    {
        refundRepository.deleteAll();
    }
}
