package com.renko.service;

import com.renko.payload.dto.InventoryDto;
import com.renko.payload.dto.updates.InventoryUpdateDto;
import com.renko.payload.response.ApiResponse;

import java.util.List;

public interface InventoryService
{
    InventoryDto createInventory(InventoryDto inventoryDto) throws Exception;
    InventoryDto updateInventory(Long id, InventoryUpdateDto inventoryDt) throws Exception;
    ApiResponse deleteInventory(Long id);
    InventoryDto getInventoryById(Long id) throws Exception;
    InventoryDto getInventoryByStoreIdAndProductId(Long productId, Long storeId);
    List<InventoryDto> getInventoryByStoreId(Long storeId);
    List<InventoryDto> getLowStockByStoreId(Long storeId);
    InventoryDto updateLowStockThreshold(Long id, Integer threshold) throws Exception;
    InventoryDto addStock(Long id, Integer quantity) throws Exception;
}
