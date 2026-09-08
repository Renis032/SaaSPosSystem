package com.renko.service.impl;

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
    public static final String USER_EMAIL = "user@renko.demo";
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
    private final SubscriptionService subscriptionService;

    @Override
    public Map<String, Object> demoInfo()
    {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("password", DEMO_PASSWORD);
        body.put("accounts", List.of(
                Map.of("role", "OWNER", "email", OWNER_EMAIL, "path", "/admin"),
                Map.of("role", "CASHIER", "email", CASHIER_EMAIL, "path", "/pos"),
                Map.of("role", "STORE_MANAGER", "email", MANAGER_EMAIL, "path", "/admin"),
                Map.of("role", "USER", "email", USER_EMAIL, "path", "/workspace")
        ));
        body.put("hint", "Use Reset demo on the landing page to wipe the DB and recreate this dataset.");
        return body;
    }

    @Override
    public Map<String, Object> resetAndSeed()
    {
        List<String> truncated = devCleanupService.clearAllTables();

        UserEntity owner = saveUser("Demo Owner", OWNER_EMAIL, "0888000100", UserRole.OWNER);
        StoreEntity store = new StoreEntity();
        store.setBrandName("Renko Demo Market");
        store.setDescription("Interview-ready demo store with sample catalog, branches, and staff.");
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
        branchIds.add(saveBranch(store, "Main Floor", "100 Market Street", "0888000201").getId());
        branchIds.add(saveBranch(store, "Cafe Corner", "100 Market Street — Cafe", "0888000202").getId());
        branchIds.add(saveBranch(store, "Warehouse Pickup", "12 Dock Road", "0888000203").getId());

        CategoryEntity beverages = saveCategory(store, "Beverages");
        CategoryEntity snacks = saveCategory(store, "Snacks");
        CategoryEntity goods = saveCategory(store, "Merchandise");

        List<Long> productIds = new ArrayList<>();
        List<Long> inventoryIds = new ArrayList<>();
        productIds.add(saveProduct(store, beverages, "House Espresso", "DEMO-ESP", "Renko", 12.5, 9.99, 80, 10, inventoryIds).getId());
        productIds.add(saveProduct(store, beverages, "Cold Brew", "DEMO-CBR", "Renko", 10.0, 7.5, 60, 8, inventoryIds).getId());
        productIds.add(saveProduct(store, snacks, "Trail Mix", "DEMO-TRM", "TrailCo", 7.0, 5.25, 45, 5, inventoryIds).getId());
        productIds.add(saveProduct(store, goods, "Branded Tote", "DEMO-TOT", "Renko", 24.0, 19.0, 14, 15, inventoryIds).getId());

        List<Long> customerIds = new ArrayList<>();
        customerIds.add(saveCustomer(store, "Walk-in Guest", "guest@renko.demo", "0888111001").getId());
        customerIds.add(saveCustomer(store, "Ada Lovelace", "ada@renko.demo", "0888111002").getId());
        customerIds.add(saveCustomer(store, "Alan Turing", "alan@renko.demo", "0888111003").getId());

        UserEntity cashier = saveUser("Demo Cashier", CASHIER_EMAIL, "0888000300", UserRole.CASHIER);
        cashier.setStoreEntity(store);
        userRepository.save(cashier);

        UserEntity manager = saveUser("Demo Manager", MANAGER_EMAIL, "0888000400", UserRole.STORE_MANAGER);
        manager.setStoreEntity(store);
        userRepository.save(manager);

        UserEntity simpleUser = saveUser("Demo User", USER_EMAIL, "0888000500", UserRole.USER);
        simpleUser.setStoreEntity(store);
        userRepository.save(simpleUser);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Demo dataset reset and seeded");
        body.put("truncatedTables", truncated.size());
        body.put("storeId", store.getId());
        body.put("branchIds", branchIds);
        body.put("productIds", productIds);
        body.put("inventoryIds", inventoryIds);
        body.put("customerIds", customerIds);
        body.put("password", DEMO_PASSWORD);
        body.put("accounts", List.of(
                Map.of("role", "OWNER", "email", OWNER_EMAIL, "userId", owner.getId()),
                Map.of("role", "CASHIER", "email", CASHIER_EMAIL, "userId", cashier.getId()),
                Map.of("role", "STORE_MANAGER", "email", MANAGER_EMAIL, "userId", manager.getId()),
                Map.of("role", "USER", "email", USER_EMAIL, "userId", simpleUser.getId())
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
}
