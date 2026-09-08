package com.renko.service.impl;

import com.renko.domain.PaymentType;
import com.renko.entities.InventoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.UserEntity;
import com.renko.exceptions.UserException;
import com.renko.payload.dto.OrderDto;
import com.renko.payload.dto.OrderItemDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.InventoryRepository;
import com.renko.repository.OrderRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.UserRepository;
import com.renko.service.BillingService;
import com.renko.service.CustomerService;
import com.renko.service.StoreAccessService;
import com.renko.service.SubscriptionService;
import com.renko.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceInventoryLockTest
{
    @Mock private OrderRepository orderRepository;
    @Mock private UserService userService;
    @Mock private ProductRepository productRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private BillingService billingService;
    @Mock private CustomerService customerService;
    @Mock private StoreRepository storeRepository;
    @Mock private UserRepository userRepository;
    @Mock private SubscriptionService subscriptionService;
    @Mock private StoreAccessService storeAccessService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private StoreEntity store;
    private UserEntity cashier;
    private ProductEntity product;
    private InventoryEntity inventory;

    @BeforeEach
    void setUp()
    {
        store = new StoreEntity();
        store.setId(10L);
        store.setBrandName("Test");
        cashier = new UserEntity();
        cashier.setId(1L);
        cashier.setStoreEntity(store);

        product = new ProductEntity();
        product.setId(5L);
        product.setName("Coffee");
        product.setSellingPrice(10.0);
        product.setDiscountPercentage(0.0);
        product.setStoreEntity(store);

        inventory = new InventoryEntity();
        inventory.setId(7L);
        inventory.setStoreEntity(store);
        inventory.setProductEntity(product);
        inventory.setQuantity(2);
        inventory.setLowStockThreshold(1);
        inventory.setVersion(0L);
    }

    @Test
    void createOrderFailsWhenStockInsufficient() throws Exception
    {
        UserDto cashierDto = new UserDto();
        cashierDto.setId(1L);
        cashierDto.setStoreId(10L);

        when(userService.getCurrentUser()).thenReturn(cashierDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByStoreAndProductForUpdate(10L, 5L)).thenReturn(Optional.of(inventory));

        OrderDto orderDto = OrderDto.builder()
                .paymentType(PaymentType.CASH)
                .items(List.of(OrderItemDto.builder().productId(5L).quantity(5).build()))
                .build();

        UserException ex = assertThrows(UserException.class, () -> orderService.createOrder(orderDto));
        assertEquals("Insufficient stock for product", ex.getMessage());
        verify(orderRepository, never()).save(any());
        verify(subscriptionService).requireActiveSubscription(10L);
    }

    @Test
    void createOrderDeductsStockUnderLock() throws Exception
    {
        UserDto cashierDto = new UserDto();
        cashierDto.setId(1L);
        cashierDto.setStoreId(10L);

        when(userService.getCurrentUser()).thenReturn(cashierDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(cashier));
        when(storeRepository.findById(10L)).thenReturn(Optional.of(store));
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByStoreAndProductForUpdate(10L, 5L)).thenReturn(Optional.of(inventory));
        when(orderRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        OrderDto orderDto = OrderDto.builder()
                .paymentType(PaymentType.CASH)
                .items(List.of(OrderItemDto.builder().productId(5L).quantity(1).build()))
                .build();

        orderService.createOrder(orderDto);

        assertEquals(1, inventory.getQuantity());
        verify(inventoryRepository).findByStoreAndProductForUpdate(10L, 5L);
        verify(inventoryRepository).save(inventory);
        verify(subscriptionService).requireActiveSubscription(anyLong());
    }
}
