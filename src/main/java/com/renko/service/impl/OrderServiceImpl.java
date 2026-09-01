package com.renko.service.impl;

import com.renko.domain.OrderStatus;
import com.renko.domain.PaymentType;
import com.renko.entities.*;
import com.renko.exceptions.UserException;
import com.renko.mapper.OrderMapper;
import com.renko.mapper.ProductMapper;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.*;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.ProductRepository;
import com.renko.service.BillingService;
import com.renko.service.CustomerService;
import com.renko.service.OrderService;
import com.renko.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.sqm.spi.CacheabilityInfluencers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService
{
    private final OrderRepository orderRepository;
    private final UserService userService;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final BillingService billingService;
    private final CustomerService customerService;

    @Override
    @Transactional
    public OrderDto createOrder(OrderDto orderDto) throws Exception
    {
        UserDto cashier = userService.getCurrentUser();
        StoreEntity store = cashier.getStoreEntity();
        if(store == null)
        {
            throw new Exception("User's store not found");
        }

        CustomerEntity customer = null;
        if(orderDto.getCustomerPhone() != null && false == orderDto.getCustomerPhone().isEmpty())
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

        else if(orderDto.getCustomer() != null)
        {
            customer = orderDto.getCustomer();
        }

        OrderEntity order = OrderEntity.builder()
                .storeEntity(store)
                .cashierEntity(UserMapper.toEntity(cashier))
                .customerEntity(customer)
                .paymentType(orderDto.getPaymentType())
                .build();

        List<OrderItemEntity> orderItems = orderDto.getItems()
                                                   .stream()
                                                   .map(itemDto ->
                                                   {
                                                      ProductEntity productEntity = productRepository.findById(itemDto.getProductId())
                                                                                                     .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                                                      double originalPrice = productEntity.getSellingPrice();
                                                      double discountPercentage = productEntity.getDiscountPercentage();

                                                      double discountAmount = (originalPrice * discountPercentage) / 100.0;
                                                      double discountedPrice = originalPrice - discountAmount;

                                                      double itemTotal = discountedPrice * itemDto.getQuantity();
                                                      double itemDiscountedTotal = discountAmount * itemDto.getQuantity();

                                                      return OrderItemEntity.builder()
                                                              .quantity(itemDto.getQuantity())
                                                              .price(itemTotal)
                                                              .originalPrice(originalPrice * itemDto.getQuantity())
                                                              .discountApplied(itemDiscountedTotal)
                                                              .productEntity(productEntity)
                                                              .orderEntity(order)
                                                              .build();
                                                   }).toList();

        double subtotal = orderItems.stream().mapToDouble(OrderItemEntity::getOriginalPrice).sum();
        double totalDiscount = orderItems.stream().mapToDouble(OrderItemEntity::getDiscountApplied).sum();
        double total = orderItems.stream().mapToDouble(OrderItemEntity::getPrice).sum();

        order.setSubtotal(subtotal);
        order.setTotalDiscount(totalDiscount);
        order.setTotalAmount(total);
        order.setItems(orderItems);

        // Verify Stripe PaymentIntent succeeded before saving
        if(orderDto.getPaymentType() == PaymentType.CARD && order.getStripePaymentIntentId() != null && false == orderDto.getStripePaymentIntentId().isBlank())
        {
            if(false == billingService.verifyPayment(orderDto.getStripePaymentIntentId()))
            {
                throw new Exception("Cart payment not confirmed");
            }

            order.setStripePaymentIntentId(orderDto.getStripePaymentIntentId());
        }

        // Validate and deduct inventory BEFORE saving order
        for(OrderItemEntity item : orderItems)
        {
            InventoryEntity inventory = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(item.getProductEntity().getId(), store.getId());

            if(inventory == null)
            {
                throw new Exception("Product " + item.getProductEntity().getName() + " is not available in inventory");
            }

            if(inventory.getQuantity() < item.getQuantity())
            {
                throw new Exception("Insufficient stock for " + item.getProductEntity().getName() + " , Available: " +
                                    inventory.getQuantity() + " but requested: " + item.getQuantity());
            }

            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryRepository.save(inventory);
        }

        OrderEntity savedOrder = orderRepository.save(order);
        return OrderMapper.toDto(savedOrder);
    }

    @Override
    public OrderDto updateOrder(Long id, OrderDto orderDto)
    {
        OrderEntity order = orderRepository.findById(id)
                                           .orElseThrow(() -> new EntityNotFoundException("Order not found with id " + id));

        if(null != orderDto.getPaymentType())
        {
            order.setPaymentType(orderDto.getPaymentType());
        }

        if(null != order.getCustomerEntity())
        {
            order.setCustomerEntity(orderDto.getCustomer());
        }

        if(orderDto.getItems() != null && false == orderDto.getItems().isEmpty())
        {
            List<OrderItemEntity> updatedItems = orderDto.getItems().stream()
                    .map(itemsDto ->
                    {
                        if(null == itemsDto.getProductDto().getId())
                        {
                            throw new EntityNotFoundException("Product id is invalid");
                        }

                        ProductEntity product = productRepository.findById(itemsDto.getProductId())
                                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

                        return OrderItemEntity.builder()
                                .id(itemsDto.getId())
                                .quantity(itemsDto.getQuantity())
                                .price(itemsDto.getPrice())
                                .originalPrice(itemsDto.getOriginalPrice())
                                .discountApplied(itemsDto.getDiscountApplied())
                                .productEntity(product)
                                .orderEntity(order)
                                .build();
                    }).collect(Collectors.toList());

            order.setItems(updatedItems);

            double total = updatedItems.stream().mapToDouble(OrderItemEntity::getPrice).sum();
            order.setTotalAmount(total);
        }

        OrderEntity saved = orderRepository.save(order);
        return OrderMapper.toDto(saved);
    }

    @Override
    public OrderDto getOrderById(Long id)
    {
        return orderRepository.findById(id)
                .map(OrderMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    @Override
    public List<OrderDto> getOrdersById(Long storeId,
                                        Long customerId,
                                        Long cashierId,
                                        PaymentType paymentType,
                                        OrderStatus orderStatus)
    {
        return orderRepository.findByStoreEntity_IdOrderByCreatedAtDesc(storeId).stream()
                .filter(order -> customerId == null || (order.getCashierEntity() != null && order.getCustomerEntity().getId().equals(customerId)))
                .filter(order -> cashierId == null || (order.getCustomerEntity() != null && order.getCustomerEntity().getId().equals(cashierId)))
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
    public void deleteOrder(Long id)
    {
        OrderEntity orderEntity = orderRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
        orderRepository.delete(orderEntity);
    }

    @Override
    public List<OrderDto> getOrdersByCustomerId(Long customerId)
    {
        return orderRepository.findByCustomerEntity_Id(customerId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTodayOrdersByStore(Long storeId)
    {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        return orderRepository.findByStoreEntity_IdAndCreatedAtBetween(storeId, start, end).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderDto> getTop5RecentOrdersByStoreId(Long storeId)
    {
        return orderRepository.findTopFiveByStoreEntity_IdOrderByCreatedAtDesc(storeId).stream()
                .map(OrderMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReceiptDto generateReceipt(Long orderId)
    {
        OrderEntity order = orderRepository.findById(orderId)
                                           .orElseThrow(() -> new EntityNotFoundException("Order not found with id " + orderId));

        String receiptNumber = String.format("RCP-%d-%d", order.getStoreEntity().getId(), order.getId());

        String storeBrand = order.getStoreEntity().getBrandName();
        String storeAddress = order.getStoreEntity().getContact().getAddress();
        String storePhone = order.getStoreEntity().getContact().getPhone();

        List<ReceiptItemDto> receiptItems = order.getItems().stream()
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

        return ReceiptDto.builder()
                .orderId(order.getId())
                .receiptNumber(receiptNumber)
                .orderDate(order.getCreatedAt())
                .storeName(storeBrand)
                .storeAddress(storeAddress)
                .storePhone(storePhone)
                .cashierName(order.getCashierEntity().getFullName())
                .customerName(order.getCustomerEntity().getFullName())
                .customerPhone(order.getCustomerEntity().getPhone())
                .items(receiptItems)
                .build();
    }
}
