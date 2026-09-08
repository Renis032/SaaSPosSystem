package com.renko.service.impl;

import com.renko.domain.OrderStatus;
import com.renko.domain.PaymentType;
import com.renko.entities.*;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.mapper.OrderMapper;
import com.renko.payload.dto.*;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderServiceImpl implements OrderService
{
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final BillingService billingService;
    private final CustomerService customerService;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
    private final StoreAccessService storeAccessService;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) throws Exception
    {
        UserDto cashierDto = userService.getCurrentUser();
        UserEntity cashier = userRepository.findById(cashierDto.getId())
                .orElseThrow(() -> ExceptionMessages.notFound("Cashier", cashierDto.getId(), "create order"));

        if(cashier.getStoreEntity() == null || cashier.getStoreEntity().getId() == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "Cashier has no linked store; cannot create order. Assign the cashier to a store first."
            );
        }

        StoreEntity store = storeRepository.findById(cashier.getStoreEntity().getId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        cashier.getStoreEntity().getId(),
                        "create order for cashierId=" + cashier.getId()
                ));

        subscriptionService.requireActiveSubscription(store.getId());

        if(orderDto.getItems() == null || orderDto.getItems().isEmpty())
        {
            throw ExceptionMessages.required(
                    "items",
                    "Order items are required. Add at least one product before creating an order."
            );
        }

        CustomerEntity customer = null;
        if(orderDto.getCustomerId() != null)
        {
            customer = customerService.getCustomer(orderDto.getCustomerId());
            if(customer.getStoreEntity() != null
               && customer.getStoreEntity().getId() != null
               && false == store.getId().equals(customer.getStoreEntity().getId()))
            {
                throw ExceptionMessages.mismatch(
                        "Customer does not belong to the cashier's store",
                        "customerId", customer.getId(),
                        "customerStoreId", customer.getStoreEntity().getId(),
                        "storeId", store.getId()
                );
            }
        }
        else if(orderDto.getCustomerPhone() != null && false == orderDto.getCustomerPhone().isEmpty())
        {
            List<CustomerEntity> existingCustomers = customerService.searchCustomer(orderDto.getCustomerPhone());

            if(false == existingCustomers.isEmpty())
            {
                customer = existingCustomers.get(0);
            }
            else
            {
                customer = new CustomerEntity();
                customer.setFullName(orderDto.getCustomerName() != null ? orderDto.getCustomerName() : "Guest");
                customer.setPhone(orderDto.getCustomerPhone());
                customer.setStoreEntity(store);
                customer = customerService.createCustomer(customer);
            }
        }

        OrderEntity order = OrderEntity.builder()
                .storeEntity(store)
                .cashierEntity(cashier)
                .customerEntity(customer)
                .paymentType(orderDto.getPaymentType())
                .build();

        List<OrderItemEntity> orderItems = new java.util.ArrayList<>();
        for(OrderItemDto itemDto : orderDto.getItems())
        {
            if(itemDto.getProductId() == null)
            {
                throw ExceptionMessages.required(
                        "productId",
                        "productId is required for each order item"
                );
            }
            if(itemDto.getQuantity() == null || itemDto.getQuantity() <= 0)
            {
                throw new IllegalArgumentException(
                        "Order item quantity must be greater than 0 for productId=" + itemDto.getProductId()
                );
            }

            ProductEntity productEntity = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Product",
                            itemDto.getProductId(),
                            "add order item with quantity=" + itemDto.getQuantity()
                    ));

            if(productEntity.getStoreEntity() == null
               || false == store.getId().equals(productEntity.getStoreEntity().getId()))
            {
                throw ExceptionMessages.mismatch(
                        "Product does not belong to the cashier's store",
                        "productId", productEntity.getId(),
                        "productStoreId", productEntity.getStoreEntity() != null
                                ? productEntity.getStoreEntity().getId()
                                : null,
                        "storeId", store.getId()
                );
            }

            double originalPrice = productEntity.getSellingPrice();
            double discountPercentage = productEntity.getDiscountPercentage();

            double discountAmount = (originalPrice * discountPercentage) / 100.0;
            double discountedPrice = originalPrice - discountAmount;

            double itemTotal = discountedPrice * itemDto.getQuantity();
            double itemDiscountedTotal = discountAmount * itemDto.getQuantity();

            orderItems.add(OrderItemEntity.builder()
                    .quantity(itemDto.getQuantity())
                    .price(itemTotal)
                    .originalPrice(originalPrice * itemDto.getQuantity())
                    .discountApplied(itemDiscountedTotal)
                    .productEntity(productEntity)
                    .orderEntity(order)
                    .build());
        }

        double subtotal = orderItems.stream().mapToDouble(OrderItemEntity::getOriginalPrice).sum();
        double totalDiscount = orderItems.stream().mapToDouble(OrderItemEntity::getDiscountApplied).sum();
        double total = orderItems.stream().mapToDouble(OrderItemEntity::getPrice).sum();

        order.setSubtotal(subtotal);
        order.setTotalDiscount(totalDiscount);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        if(orderDto.getPaymentType() == PaymentType.CARD)
        {
            String intentId = orderDto.getStripePaymentIntentId();
            if(intentId == null || intentId.isBlank())
            {
                throw new Exception("CARD payment requires stripePaymentIntentId (use demo_… for local demo card)");
            }

            boolean demoCard = intentId.startsWith("demo_");
            if(false == demoCard && false == billingService.verifyPayment(intentId))
            {
                throw new Exception("CARD payment not confirmed for stripePaymentIntentId="
                        + intentId
                        + "; storeId=" + store.getId()
                        + ", cashierId=" + cashier.getId());
            }

            order.setStripePaymentIntentId(intentId);
        }

        for(OrderItemEntity item : orderItems)
        {
            InventoryEntity inventory = inventoryRepository
                    .findByStoreAndProductForUpdate(store.getId(), item.getProductEntity().getId())
                    .orElse(null);

            if(inventory == null)
            {
                throw UserException.withDetails(
                        "Product has no inventory row for this store; create inventory before ordering",
                        ExceptionMessages.ctx(
                                "productId", item.getProductEntity().getId(),
                                "productName", item.getProductEntity().getName(),
                                "storeId", store.getId()
                        )
                );
            }

            if(inventory.getQuantity() < item.getQuantity())
            {
                throw UserException.withDetails(
                        "Insufficient stock for product",
                        ExceptionMessages.ctx(
                                "productId", item.getProductEntity().getId(),
                                "productName", item.getProductEntity().getName(),
                                "storeId", store.getId(),
                                "available", inventory.getQuantity(),
                                "requested", item.getQuantity()
                        )
                );
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        OrderEntity detailed = orderRepository.findDetailedById(savedOrder.getId()).orElse(savedOrder);
        auditLogService.record(
                store.getId(),
                "ORDER_CREATE",
                "Order",
                String.valueOf(detailed.getId()),
                "paymentType=" + orderDto.getPaymentType() + "; total=" + detailed.getTotalAmount()
        );
        return OrderMapper.toDto(detailed);
    }

    @Override
    @Transactional
    public OrderDto updateOrder(Long id, OrderDto orderDto) throws Exception
    {
        OrderEntity order = orderRepository.findById(id)
                                           .orElseThrow(() -> ExceptionMessages.notFound("Order", id, "update"));

        if(null != orderDto.getPaymentType())
        {
            order.setPaymentType(orderDto.getPaymentType());
        }

        if(orderDto.getCustomerId() != null)
        {
            order.setCustomerEntity(customerService.getCustomer(orderDto.getCustomerId()));
        }

        if(orderDto.getItems() != null && false == orderDto.getItems().isEmpty())
        {
            List<OrderItemEntity> updatedItems = new java.util.ArrayList<>();
            for(OrderItemDto itemsDto : orderDto.getItems())
            {
                if(null == itemsDto.getProductId())
                {
                    throw ExceptionMessages.required(
                            "productId",
                            "Order item productId is required while updating orderId=" + id
                    );
                }

                ProductEntity product = productRepository.findById(itemsDto.getProductId())
                        .orElseThrow(() -> ExceptionMessages.notFound(
                                "Product",
                                itemsDto.getProductId(),
                                "update orderId=" + id
                        ));

                updatedItems.add(OrderItemEntity.builder()
                        .id(itemsDto.getId())
                        .quantity(itemsDto.getQuantity())
                        .price(itemsDto.getPrice())
                        .originalPrice(itemsDto.getOriginalPrice())
                        .discountApplied(itemsDto.getDiscountApplied())
                        .productEntity(product)
                        .orderEntity(order)
                        .build());
            }

            order.setItems(updatedItems);

            double total = updatedItems.stream().mapToDouble(OrderItemEntity::getPrice).sum();
            order.setTotalAmount(total);
        }

        OrderEntity saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id)
    {
        return orderRepository.findDetailedById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> ExceptionMessages.notFound("Order", id));
    }

    @Override
    public List<OrderDto> getAllOrders()
    {
        return orderRepository.findAll().stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByStore(Long storeId,
                                           Long customerId,
                                           Long cashierId,
                                           PaymentType paymentType,
                                           OrderStatus orderStatus) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return orderRepository.findByStoreEntity_IdOrderByCreatedAtDesc(storeId).stream()
                .filter(order -> customerId == null || (order.getCustomerEntity() != null && order.getCustomerEntity().getId().equals(customerId)))
                .filter(order -> cashierId == null || (order.getCashierEntity() != null && order.getCashierEntity().getId().equals(cashierId)))
                .filter(order -> paymentType == null || order.getPaymentType() == paymentType)
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getOrdersByCashier(Long cashierId)
    {
        return orderRepository.findByCashierEntity_Id(cashierId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteOrder(Long id)
    {
        OrderEntity orderEntity = orderRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Order", id, "delete"));
        orderRepository.delete(orderEntity);
    }

    @Override
    @Transactional
    public void deleteAllOrders()
    {
        orderRepository.deleteAll();
    }

    @Override
    public List<OrderDto> getOrdersByCustomerId(Long customerId)
    {
        return orderRepository.findByCustomerEntity_Id(customerId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTodayOrdersByStore(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository.findByStoreEntity_IdAndCreatedAtBetween(storeId, start, end).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTop5RecentOrdersByStoreId(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return orderRepository.findTopFiveByStoreEntity_IdOrderByCreatedAtDesc(storeId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ReceiptDto getReceipt(Long orderId)
    {
        OrderEntity order = orderRepository.findDetailedById(orderId)
                                           .orElseThrow(() -> ExceptionMessages.notFound("Order", orderId, "build receipt"));

        if(order.getStoreEntity() == null)
        {
            throw ExceptionMessages.notFound("Store", null, "build receipt for orderId=" + orderId);
        }

        String receiptNumber = String.format("RCP-%d-%d", order.getStoreEntity().getId(), order.getId());

        String storeBrand = order.getStoreEntity().getBrandName();
        StoreContactEntity contact = order.getStoreEntity().getContact();
        String storeAddress = contact != null ? contact.getAddress() : null;
        String storePhone = contact != null ? contact.getPhone() : null;

        List<OrderItemEntity> items = order.getItems() != null ? order.getItems() : List.of();
        List<ReceiptItemDto> receiptItems = items.stream()
                .map(item ->
                {
                   ProductEntity product = item.getProductEntity();

                   double itemOriginalPrice = item.getOriginalPrice();
                   double itemDiscountApplied = item.getDiscountApplied();
                   double itemPrice = item.getPrice();
                   int quantity = item.getQuantity();

                   double originalPricePerUnit = quantity > 0 ? itemOriginalPrice / quantity : 0.0;
                   double discountAmountPerUnit = quantity > 0 ? itemDiscountApplied / quantity : 0.0;
                   double finalPricePerUnit = quantity > 0 ? itemPrice / quantity : 0.0;
                   double discountPercentage = (product != null) && (product.getDiscountPercentage() != null) ? product.getDiscountPercentage() : 0.0;


                   return ReceiptItemDto.builder()
                           .name(product != null ? product.getName() : "Unknown product")
                           .sku(product != null ? product.getSku() : "")
                           .quantity(quantity)
                           .originalPrice(originalPricePerUnit)
                           .discountPercentage(discountPercentage)
                           .discountAmount(discountAmountPerUnit)
                           .finalPrice(finalPricePerUnit)
                           .lineTotal(itemPrice)
                           .build();
                }).collect(Collectors.toList());

        String cashierName = order.getCashierEntity() != null ? order.getCashierEntity().getFullName() : null;
        String customerName = order.getCustomerEntity() != null ? order.getCustomerEntity().getFullName() : null;
        String customerPhone = order.getCustomerEntity() != null ? order.getCustomerEntity().getPhone() : null;

        return ReceiptDto.builder()
                .orderId(order.getId())
                .receiptNumber(receiptNumber)
                .orderDate(order.getCreatedAt())
                .storeName(storeBrand)
                .storeAddress(storeAddress)
                .storePhone(storePhone)
                .cashierName(cashierName)
                .customerName(customerName)
                .customerPhone(customerPhone)
                .items(receiptItems)
                .build();
    }
}
