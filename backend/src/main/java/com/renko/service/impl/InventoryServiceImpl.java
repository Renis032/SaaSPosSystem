package com.renko.service.impl;

import com.renko.entities.InventoryEntity;
import com.renko.entities.ProductEntity;
import com.renko.entities.StoreEntity;
import com.renko.mapper.InventoryMapper;
import com.renko.payload.dto.InventoryDto;
import com.renko.payload.dto.updates.InventoryUpdateDto;
import com.renko.payload.response.ApiResponse;
import com.renko.repository.InventoryRepository;
import com.renko.repository.ProductRepository;
import com.renko.repository.StoreRepository;
import com.renko.service.InventoryService;
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

    @Override
    public InventoryDto createInventory(InventoryDto inventoryDto) throws Exception
    {
        StoreEntity storeEntity = storeRepository.findById(inventoryDto.getStoreId())
                                                 .orElseThrow(() -> new Exception("Store not found with id: " + inventoryDto.getStoreId()
                                                         + "; cannot create inventory for productId=" + inventoryDto.getProductId()));

        ProductEntity productEntity = productRepository.findById(inventoryDto.getProductId())
                                                       .orElseThrow(() -> new Exception("Product not found with id: " + inventoryDto.getProductId()
                                                               + "; cannot create inventory for storeId=" + inventoryDto.getStoreId()));

        InventoryEntity inventoryEntity = InventoryMapper.toEntity(inventoryDto, storeEntity, productEntity);
        InventoryEntity savedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(savedInventory);
    }

    @Override
    public InventoryDto updateInventory(Long id, InventoryUpdateDto inventoryDto) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Inventory not found with id: " + id + "; cannot update"));

        if(inventoryDto.getStoreId() != null)
        {
            inventoryEntity.setStoreEntity(storeRepository.findById(inventoryDto.getStoreId())
                                                          .orElseThrow(() -> new Exception("Store not found with id: " + inventoryDto.getStoreId()
                                                                  + "; cannot update inventoryId=" + id)));
        }

        if(inventoryDto.getProductDto() != null)
        {
            inventoryEntity.setProductEntity(productRepository.findById(inventoryDto.getProductId())
                    .orElseThrow(() -> new Exception("Product not found with id: " + inventoryDto.getProductId()
                            + "; cannot update inventoryId=" + id)));
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
    public ApiResponse deleteInventory(Long id)
    {
        inventoryRepository.deleteById(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Inventory deleted successfully");

        return apiResponse;
    }

    @Override
    public InventoryDto getInventoryById(Long id) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                                                             .orElseThrow(() -> new Exception("Inventory not found with id: " + id));

        return InventoryMapper.toDto(inventoryEntity);
    }

    @Override
    public InventoryDto getInventoryByStoreIdAndProductId(Long storeId, Long productId)
    {
        InventoryEntity inventoryEntity = inventoryRepository.findByStoreEntity_IdAndProductEntity_Id(storeId, productId);
        return inventoryEntity != null ? InventoryMapper.toDto(inventoryEntity) : null;
    }

    @Override
    public List<InventoryDto> getInventoryByStoreId(Long storeId)
    {
        return inventoryRepository.findByStoreEntity_Id(storeId).stream()
                .map(InventoryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InventoryDto> getLowStockByStoreId(Long storeId)
    {
        return inventoryRepository.findByStoreEntity_Id(storeId).stream()
                .filter(inv -> inv.getQuantity() <= inv.getLowStockThreshold())
                .map(InventoryMapper::toDto)
                .sorted((a, b) -> Integer.compare(a.getQuantity(), b.getQuantity())) // Lowest first
                .collect(Collectors.toList());
    }

    @Override
    public InventoryDto updateLowStockThreshold(Long id, Integer threshold) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                                                             .orElseThrow(() -> new Exception("Inventory not found with id: " + id
                                                                     + "; cannot set lowStockThreshold=" + threshold));

        inventoryEntity.setLowStockThreshold(threshold);
        InventoryEntity updatedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(updatedInventory);
    }

    @Override
    public InventoryDto addStock(Long id, Integer quantity) throws Exception
    {
        InventoryEntity inventoryEntity = inventoryRepository.findById(id)
                .orElseThrow(() -> new Exception("Inventory not found with id: " + id
                        + "; cannot addStock quantity=" + quantity));

        inventoryEntity.setQuantity(inventoryEntity.getQuantity() + quantity);
        InventoryEntity updatedInventory = inventoryRepository.save(inventoryEntity);

        return InventoryMapper.toDto(updatedInventory);
    }
}
