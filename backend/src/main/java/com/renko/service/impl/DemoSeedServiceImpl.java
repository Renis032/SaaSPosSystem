package com.renko.service.impl;

import com.renko.domain.PaymentType;
import com.renko.domain.StoreStatus;
import com.renko.domain.SubscriptionPlan;
import com.renko.domain.UserRole;
import com.renko.entities.*;
import com.renko.repository.*;
import com.renko.service.DemoSeedService;
import com.renko.service.DevCleanupService;
import com.renko.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Profile("dev")
@RequiredArgsConstructor
public class DemoSeedServiceImpl implements DemoSeedService
{
    public static final String OWNER_EMAIL = "owner@renko.demo";
    public static final String CASHIER_EMAIL = "cashier@renko.demo";
    public static final String MANAGER_EMAIL = "manager@renko.demo";
    public static final String DEMO_PASSWORD = "Demo1234!";

    private final DevCleanupService devCleanupService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;
    private final BranchRepository branchRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final AuditLogRepository auditLogRepository;
    private final SubscriptionService subscriptionService;
    private final PlatformTransactionManager transactionManager;

    @Override
    public Map<String, Object> demoInfo()
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", DEMO_PASSWORD);
        body.put("accounts", List.of(
                Map.of("role", "CASHIER", "email", CASHIER_EMAIL, "path", "/pos"),
                Map.of("role", "OWNER", "email", OWNER_EMAIL, "path", "/admin"),
                Map.of("role", "STORE_MANAGER", "email", MANAGER_EMAIL, "path", "/admin")
        ));
        body.put("hint", "Use Reset demo on the landing page to wipe the DB and recreate this dataset.");
        return body;
    }

    @Override
    public Map<String, Object> resetAndSeed()
    {
        // Clear must commit before any ID allocation (see DevCleanupServiceImpl).
        List<String> truncated = devCleanupService.clearAllTables();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        Map<String, Object> seeded = tx.execute(status -> seedDemoData(truncated));
        if (seeded == null)
        {
            throw new IllegalStateException("Demo seed transaction returned no result");
        }
        return seeded;
    }

    private Map<String, Object> seedDemoData(List<String> truncated)
    {
        UserEntity owner = saveUser("Demo Owner", OWNER_EMAIL, "0888000100", UserRole.OWNER);
        StoreEntity store = new StoreEntity();
        store.setBrandName("Renko Demo Market");
        store.setDescription("Demo store with sample catalog, branches, and staff.");
        store.setStoreType("RETAIL");
        store.setStoreAdmin(owner);
        store.setContact(StoreContactEntity.builder()
                .email(OWNER_EMAIL)
                .phone("0888000100")
                .address("100 Market Street")
                .build());
        store = storeRepository.save(store);
        store.setStatus(StoreStatus.ACTIVE);
        store = storeRepository.save(store);

        owner.setStoreEntity(store);
        userRepository.save(owner);

        subscriptionService.createTrialForNewStore(store, SubscriptionPlan.STARTER);
        subscriptionService.activateFromStripeWebhook(store.getId(), "demo_customer", "demo_sub");

        List<Long> branchIds = new ArrayList<>();
        List<BranchEntity> branches = new ArrayList<>();
        branches.add(saveBranch(store, "Main Floor", "100 Market Street", "0888000201"));
        branches.add(saveBranch(store, "Cafe Corner", "100 Market Street — Cafe", "0888000202"));
        branches.add(saveBranch(store, "Warehouse Pickup", "12 Dock Road", "0888000203"));
        for (BranchEntity branch : branches)
        {
            branchIds.add(branch.getId());
        }

        CategoryEntity beverages = saveCategory(store, "Beverages");
        CategoryEntity snacks = saveCategory(store, "Snacks");
        CategoryEntity goods = saveCategory(store, "Merchandise");

        List<Long> productIds = new ArrayList<>();
        List<Long> inventoryIds = new ArrayList<>();
        List<ProductEntity> products = new ArrayList<>();
        products.add(saveProduct(store, beverages, "House Espresso", "DEMO-ESP", "Renko", 12.5, 9.99, 80, 10, inventoryIds));
        products.add(saveProduct(store, beverages, "Cold Brew", "DEMO-CBR", "Renko", 10.0, 7.5, 60, 8, inventoryIds));
        products.add(saveProduct(store, snacks, "Trail Mix", "DEMO-TRM", "TrailCo", 7.0, 5.25, 45, 5, inventoryIds));
        products.add(saveProduct(store, goods, "Branded Tote", "DEMO-TOT", "Renko", 24.0, 19.0, 14, 15, inventoryIds));
        for (ProductEntity product : products)
        {
            productIds.add(product.getId());
        }

        List<CustomerEntity> customers = new ArrayList<>();
        customers.add(saveCustomer(store, "Walk-in Guest", "guest@renko.demo", "0888111001"));
        customers.add(saveCustomer(store, "Ada Lovelace", "ada@renko.demo", "0888111002"));
        customers.add(saveCustomer(store, "Alan Turing", "alan@renko.demo", "0888111003"));
        List<Long> customerIds = customers.stream().map(CustomerEntity::getId).toList();

        UserEntity cashier = saveUser("Demo Cashier", CASHIER_EMAIL, "0888000300", UserRole.CASHIER);
        cashier.setStoreEntity(store);
        userRepository.save(cashier);

        UserEntity manager = saveUser("Demo Manager", MANAGER_EMAIL, "0888000400", UserRole.STORE_MANAGER);
        manager.setStoreEntity(store);
        userRepository.save(manager);

        List<Long> orderIds = seedSampleOrders(store, branches, products, customers, cashier);

        auditLogRepository.save(AuditLogEntity.builder()
                .storeId(store.getId())
                .actorUserId(owner.getId())
                .actorEmail(OWNER_EMAIL)
                .action("PRODUCT_UPDATE")
                .entityType("Product")
                .entityId(String.valueOf(products.get(0).getId()))
                .beforeState("name=House Espresso; sellingPrice=9.99; discountPercentage=0.0")
                .afterState("name=House Espresso; sellingPrice=9.49; discountPercentage=5.0")
                .details("Demo seed sample price change")
                .build());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Demo dataset reset and seeded");
        body.put("truncatedTables", truncated.size());
        body.put("storeId", store.getId());
        body.put("branchIds", branchIds);
        body.put("productIds", productIds);
        body.put("inventoryIds", inventoryIds);
        body.put("customerIds", customerIds);
        body.put("orderIds", orderIds);
        body.put("password", DEMO_PASSWORD);
        body.put("accounts", List.of(
                Map.of("role", "CASHIER", "email", CASHIER_EMAIL, "userId", cashier.getId()),
                Map.of("role", "OWNER", "email", OWNER_EMAIL, "userId", owner.getId()),
                Map.of("role", "STORE_MANAGER", "email", MANAGER_EMAIL, "userId", manager.getId())
        ));
        return body;
    }

    private UserEntity saveUser(String name, String email, String phone, UserRole role)
    {
        UserEntity user = new UserEntity();
        user.setFullName(name);
        user.setEmail(email);
        user.setPhoneNumber(phone);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(DEMO_PASSWORD));
        return userRepository.save(user);
    }

    private BranchEntity saveBranch(StoreEntity store, String name, String address, String phone)
    {
        return branchRepository.save(BranchEntity.builder()
                .name(name)
                .address(address)
                .phone(phone)
                .email(name.toLowerCase().replace(' ', '.') + "@renko.demo")
                .storeEntity(store)
                .openTime(LocalTime.of(9, 0))
                .closeTime(LocalTime.of(21, 0))
                .workdays(List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY"))
                .build());
    }

    private CategoryEntity saveCategory(StoreEntity store, String name)
    {
        return categoryRepository.save(CategoryEntity.builder()
                .name(name)
                .storeEntity(store)
                .build());
    }

    private ProductEntity saveProduct(StoreEntity store,
                                      CategoryEntity category,
                                      String name,
                                      String sku,
                                      String brand,
                                      double mrp,
                                      double price,
                                      int qty,
                                      int lowStock,
                                      List<Long> inventoryIds)
    {
        ProductEntity product = productRepository.save(ProductEntity.builder()
                .name(name)
                .sku(sku)
                .description("Demo product — " + name)
                .brand(brand)
                .imageUrl("")
                .maxRetailPrice(mrp)
                .sellingPrice(price)
                .discountPercentage(0.0)
                .storeEntity(store)
                .categoryEntity(category)
                .build());

        InventoryEntity inventory = inventoryRepository.save(InventoryEntity.builder()
                .storeEntity(store)
                .productEntity(product)
                .quantity(qty)
                .lowStockThreshold(lowStock)
                .version(0L)
                .build());
        inventoryIds.add(inventory.getId());
        return product;
    }

    private CustomerEntity saveCustomer(StoreEntity store, String name, String email, String phone)
    {
        return customerRepository.save(CustomerEntity.builder()
                .fullName(name)
                .email(email)
                .phone(phone)
                .storeEntity(store)
                .build());
    }

    private List<Long> seedSampleOrders(StoreEntity store,
                                        List<BranchEntity> branches,
                                        List<ProductEntity> products,
                                        List<CustomerEntity> customers,
                                        UserEntity cashier)
    {
        List<Long> orderIds = new ArrayList<>();
        if (products.isEmpty() || branches.isEmpty())
        {
            return orderIds;
        }

        PaymentType[] payments = {PaymentType.CASH, PaymentType.CARD, PaymentType.UPI};
        int[] dayOffsets = {0, 1, 2, 3, 4, 5, 6};
        int[] qtyByDay = {2, 1, 3, 1, 2, 1, 2};

        for (int i = 0; i < dayOffsets.length; i++)
        {
            ProductEntity product = products.get(i % products.size());
            BranchEntity branch = branches.get(i % branches.size());
            CustomerEntity customer = customers.isEmpty() ? null : customers.get(i % customers.size());
            int qty = qtyByDay[i];
            double unitPrice = product.getSellingPrice() != null ? product.getSellingPrice() : 0.0;
            double subtotal = unitPrice * qty;
            double taxRate = i % 2 == 0 ? 8.0 : 0.0;
            double taxAmount = Math.round(subtotal * taxRate) / 100.0;
            double orderDiscountPercent = i == 2 ? 5.0 : 0.0;
            double discountAmount = Math.round(subtotal * orderDiscountPercent) / 100.0;
            double total = Math.max(0.0, subtotal - discountAmount + taxAmount);

            OrderItemEntity item = OrderItemEntity.builder()
                    .productEntity(product)
                    .quantity(qty)
                    .price(unitPrice)
                    .originalPrice(product.getMaxRetailPrice())
                    .discountApplied(0.0)
                    .build();

            List<OrderItemEntity> items = new ArrayList<>();
            items.add(item);

            OrderEntity order = OrderEntity.builder()
                    .storeEntity(store)
                    .branchEntity(branch)
                    .cashierEntity(cashier)
                    .customerEntity(customer)
                    .paymentType(payments[i % payments.length])
                    .subtotal(subtotal)
                    .totalDiscount(discountAmount)
                    .orderDiscountPercent(orderDiscountPercent)
                    .taxRate(taxRate)
                    .taxAmount(taxAmount)
                    .totalAmount(total)
                    .stripePaymentIntentId(payments[i % payments.length] == PaymentType.CARD ? "demo_seed_" + i : null)
                    .items(items)
                    .build();
            item.setOrderEntity(order);

            OrderEntity saved = orderRepository.save(order);
            saved.setCreatedAt(LocalDateTime.now().minusDays(dayOffsets[i]).withHour(10 + i).withMinute(15));
            saved = orderRepository.save(saved);
            orderIds.add(saved.getId());

            InventoryEntity inv = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(
                    store.getId(), product.getId());
            if (inv != null)
            {
                inv.setQuantity(Math.max(0, inv.getQuantity() - qty));
                inventoryRepository.save(inv);
            }
        }
        return orderIds;
    }
}
