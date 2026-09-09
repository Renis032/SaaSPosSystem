package com.renko.service.impl;

import com.renko.entities.InventoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.exceptions.ExceptionMessages;
import com.renko.exceptions.UserException;
import com.renko.mapper.InventoryMapper;
import com.renko.payload.dto.InventoryDto;
import com.renko.payload.dto.updates.InventoryUpdateDto;
import com.renko.payload.response.ApiResponse;
import com.renko.repository.InventoryRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.service.AuditLogService;
import com.renko.service.InventoryService;
import com.renko.service.StoreAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService
{
    private final InventoryRepository inventoryRepository;
    private final StoreRepository storeRepository;
    private final ProductRepository productRepository;
    private final StoreAccessService storeAccessService;
    private final AuditLogService auditLogService;

    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) throws Exception
    {
        if(inventoryDto.getStoreId() == null)
        {
            throw ExceptionMessages.required(
                    "storeId",
                    "storeId is required to create inventory. Create a store first."
            );
        }
        if(inventoryDto.getProductId() == null)
        {
            throw ExceptionMessages.required(
                    "productId",
                    "productId is required to create inventory. Create a product first."
            );
        }

        storeAccessService.requireStoreAccess(inventoryDto.getStoreId());

        StoreEntity storeEntity = storeRepository.findById(inventoryDto.getStoreId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Store",
                        inventoryDto.getStoreId(),
                        "create inventory for productId=" + inventoryDto.getProductId()
                ));

        ProductEntity productEntity = productRepository.findById(inventoryDto.getProductId())
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Product",
                        inventoryDto.getProductId(),
                        "create inventory for storeId=" + inventoryDto.getStoreId()
                ));

        assertProductBelongsToStore(productEntity, storeEntity.getId(), "create inventory");

        InventoryEntity inventoryEntity = InventoryMapper.toEntity(inventoryDto, storeEntity, productEntity);
        InventoryEntity savedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(savedInventory);
    }

    @Override
    public InventoryDto updateInventory(Long id, InventoryUpdateDto inventoryDto) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Inventory", id, "update"));

        if(inventoryDto.getStoreId() != null)
        {
            inventoryEntity.setStoreEntity(storeRepository.findById(inventoryDto.getStoreId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Store",
                            inventoryDto.getStoreId(),
                            "update inventoryId=" + id
                    )));
        }

        if(inventoryDto.getProductId() != null)
        {
            ProductEntity productEntity = productRepository.findById(inventoryDto.getProductId())
                    .orElseThrow(() -> ExceptionMessages.notFound(
                            "Product",
                            inventoryDto.getProductId(),
                            "update inventoryId=" + id
                    ));
            Long storeId = inventoryEntity.getStoreEntity() != null
                    ? inventoryEntity.getStoreEntity().getId()
                    : null;
            assertProductBelongsToStore(productEntity, storeId, "update inventoryId=" + id);
            inventoryEntity.setProductEntity(productEntity);
        }

        if(inventoryDto.getQuantity() != null)
        {
            inventoryEntity.setQuantity(inventoryDto.getQuantity());
        }

        if(inventoryDto.getLowStockThreshold() != null)
        {
            inventoryEntity.setLowStockThreshold(inventoryDto.getLowStockThreshold());
        }

        inventoryEntity.setLastUpdated(LocalDateTime.now());
        InventoryEntity savedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(savedInventory);
    }

    @Override
    public ApiResponse deleteInventory(Long id) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Inventory", id, "delete"));
        inventoryRepository.delete(inventoryEntity);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Inventory deleted successfully");

        return apiResponse;
    }

    @Override
    public void deleteAllInventories()
    {
        inventoryRepository.deleteAll();
    }

    @Override
    public InventoryDto getInventoryById(Long id) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Inventory", id));

        return InventoryMapper.toDto(inventoryEntity);
    }

    @Override
    public List<InventoryDto> getAllInventories()
    {
        return inventoryRepository.findAll().stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getInventoryByStoreId(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return inventoryRepository.findByStoreEntity_Id(storeId).stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getLowStockByStoreId(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return inventoryRepository.findByStoreEntity_Id(storeId).stream()
                .filter(inv -> inv.getQuantity() <= inv.getLowStockThreshold())
                .map(InventoryMapper::toDto)
                .sorted((a, b) -> Integer.compare(a.getQuantity(), b.getQuantity()))
                .collect(Collectors.toList());
    }

    @Override
    public InventoryDto getInventoryByStoreIdAndProductId(Long storeId, Long productId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        InventoryEntity inventoryEntity = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(storeId, productId);
        return inventoryEntity != null ? InventoryMapper.toDto(inventoryEntity) : null;
    }

    @Override
    public InventoryDto updateLowStockThreshold(Long id, Integer threshold) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Inventory",
                        id,
                        "set lowStockThreshold=" + threshold
                ));

        inventoryEntity.setLowStockThreshold(threshold);
        InventoryEntity updatedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(updatedInventory);
    }

    @Override
    public InventoryDto addStock(Long id, Integer quantity) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound(
                        "Inventory",
                        id,
                        "addStock quantity=" + quantity
                ));

        if(inventoryEntity.getStoreEntity() != null)
        {
            storeAccessService.requireStoreAccess(inventoryEntity.getStoreEntity().getId());
        }

        int previous = inventoryEntity.getQuantity();
        inventoryEntity.setQuantity(inventoryEntity.getQuantity() + quantity);
        InventoryEntity updatedInventory = inventoryRepository.save(inventoryEntity);
        auditLogService.recordChange(
                inventoryEntity.getStoreEntity() != null ? inventoryEntity.getStoreEntity().getId() : null,
                "INVENTORY_ADD",
                "Inventory",
                String.valueOf(id),
                "qty=" + previous,
                "qty=" + updatedInventory.getQuantity(),
                "Added quantity=" + quantity
        );

        return InventoryMapper.toDto(updatedInventory);
    }

    @Override
    public InventoryDto adjustStock(Long id, Integer delta, String reason) throws Exception
    {
        if(delta == null || delta == 0)
        {
            throw ExceptionMessages.required("delta", "Stock adjustment delta must be a non-zero integer");
        }

        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> ExceptionMessages.notFound("Inventory", id, "adjust stock"));

        if(inventoryEntity.getStoreEntity() != null)
        {
            storeAccessService.requireStoreAccess(inventoryEntity.getStoreEntity().getId());
        }

        int next = inventoryEntity.getQuantity() + delta;
        if(next < 0)
        {
            throw UserException.withDetails(
                    "Adjustment would make stock negative",
                    ExceptionMessages.ctx(
                            "inventoryId", id,
                            "current", inventoryEntity.getQuantity(),
                            "delta", delta
                    )
            );
        }

        int previous = inventoryEntity.getQuantity();
        inventoryEntity.setQuantity(next);
        InventoryEntity saved = inventoryRepository.save(inventoryEntity);
        auditLogService.recordChange(
                inventoryEntity.getStoreEntity() != null ? inventoryEntity.getStoreEntity().getId() : null,
                "INVENTORY_ADJUST",
                "Inventory",
                String.valueOf(id),
                "qty=" + previous,
                "qty=" + next,
                "delta=" + delta + (reason != null ? "; reason=" + reason : "")
        );
        return InventoryMapper.toDto(saved);
    }

    private void assertProductBelongsToStore(ProductEntity productEntity, Long storeId, String action)
            throws Exception
    {
        if(storeId == null)
        {
            return;
        }
        if(productEntity.getStoreEntity() == null
           || false == storeId.equals(productEntity.getStoreEntity().getId()))
        {
            throw ExceptionMessages.mismatch(
                    "Product does not belong to the inventory store; cannot " + action,
                    "productId", productEntity.getId(),
                    "productStoreId", productEntity.getStoreEntity() != null
                            ? productEntity.getStoreEntity().getId()
                            : null,
                    "storeId", storeId
            );
        }
    }
}
