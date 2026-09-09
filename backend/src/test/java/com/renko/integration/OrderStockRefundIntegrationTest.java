package com.renko.integration;

import com.renko.domain.PaymentType;
import com.renko.domain.SubscriptionPlan;
import com.renko.domain.SubscriptionStatus;
import com.renko.domain.UserRole;
import com.renko.entities.InventoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.entities.StoreSubscriptionEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.OrderDto;
import com.renko.payload.dto.OrderItemDto;
import com.renko.payload.dto.RefundDto;
import com.renko.payload.dto.UserDto;
import com.renko.repository.InventoryRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.repository.StoreSubscriptionRepository;
import com.renko.repository.UserRepository;
import com.renko.service.OrderService;
import com.renko.service.RefundService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
@ActiveProfiles("test")
class OrderStockRefundIntegrationTest
{
    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private OrderService orderService;
    @Autowired
    private RefundService refundService;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private StoreSubscriptionRepository subscriptionRepository;

    @MockitoBean
    private UserService userService;
    @MockitoBean
    private StoreAccessService storeAccessService;

    private StoreEntity store;
    private UserEntity cashier;
    private ProductEntity product;
    private InventoryEntity inventory;

    @BeforeEach
    void setUp() throws Exception
    {
        doNothing().when(storeAccessService).requireStoreAccess(anyLong());

        store = new StoreEntity();
        store.setBrandName("IT Store");
        store = storeRepository.save(store);

        cashier = new UserEntity();
        cashier.setEmail("cashier-it-" + System.nanoTime() + "@renko.test");
        cashier.setFullName("IT Cashier");
        cashier.setPassword("x");
        cashier.setRole(UserRole.CASHIER);
        cashier.setStoreEntity(store);
        cashier = userRepository.save(cashier);

        subscriptionRepository.save(StoreSubscriptionEntity.builder()
                .storeEntity(store)
                .plan(SubscriptionPlan.STARTER)
                .status(SubscriptionStatus.ACTIVE)
                .currentPeriodEnd(LocalDateTime.now().plusDays(30))
                .build());

        product = productRepository.save(ProductEntity.builder()
                .name("Espresso")
                .sku("IT-ESP-" + System.nanoTime())
                .sellingPrice(5.0)
                .discountPercentage(0.0)
                .storeEntity(store)
                .build());

        inventory = inventoryRepository.save(InventoryEntity.builder()
                .storeEntity(store)
                .productEntity(product)
                .quantity(10)
                .lowStockThreshold(2)
                .version(0L)
                .build());

        UserDto current = new UserDto();
        current.setId(cashier.getId());
        current.setEmail(cashier.getEmail());
        current.setStoreId(store.getId());
        current.setRole(UserRole.CASHIER);
        when(userService.getCurrentUser()).thenReturn(current);
    }

    @Test
    void createOrderDeductsStockAndRefundRestoresIt() throws Exception
    {
        OrderDto created = orderService.createOrder(OrderDto.builder()
                .paymentType(PaymentType.CASH)
                .taxRate(10.0)
                .orderDiscountPercent(0.0)
                .items(List.of(OrderItemDto.builder().productId(product.getId()).quantity(3).build()))
                .build());

        InventoryEntity afterSale = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(7, afterSale.getQuantity());
        assertEquals(15.0, created.getSubtotal(), 0.01);
        assertEquals(1.5, created.getTaxAmount(), 0.01);
        assertEquals(16.5, created.getTotalAmount(), 0.01);

        refundService.createRefund(RefundDto.builder()
                .orderId(created.getId())
                .amount(created.getTotalAmount())
                .reason("integration test refund")
                .paymentType(PaymentType.CASH)
                .build());

        InventoryEntity afterRefund = inventoryRepository.findById(inventory.getId()).orElseThrow();
        assertEquals(10, afterRefund.getQuantity());
    }
}
