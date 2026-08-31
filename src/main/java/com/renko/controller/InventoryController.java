package com.renko.controller;

import com.renko.payload.dto.InventoryDto;
import com.renko.payload.dto.updates.InventoryUpdateDto;
import com.renko.payload.response.ApiResponse;
import com.renko.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inventories")
public class InventoryController
{
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<InventoryDto> create(@RequestBody InventoryDto inventoryDto) throws Exception
    {
        return ResponseEntity.ok(inventoryService.createInventory(inventoryDto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryDto> update(@RequestBody InventoryUpdateDto inventoryDto,
                                               @PathVariable Long id) throws Exception
    {
        return ResponseEntity.ok(inventoryService.updateInventory(id, inventoryDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> delete(@PathVariable Long id)
    {
        inventoryService.deleteInventory(id);

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("Inventory deleted successfully");
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<List<InventoryDto>> getInventoryByStoreId(@PathVariable Long storeId)
    {
        return ResponseEntity.ok(inventoryService.getInventoryByStoreId(storeId));
    }

    @GetMapping("/store/{storeId}/product/{productId}")
    public ResponseEntity<InventoryDto> getInventoryByStoreIdAndProductId(@PathVariable Long storeId,
                                                                          @PathVariable Long productId)
    {
        return ResponseEntity.ok(inventoryService.getInventoryByStoreIdAndProductId(storeId, productId));
    }

    @GetMapping("/store/{storeId}/low-stock")
    public ResponseEntity<List<InventoryDto>> getLowStockInventory(@PathVariable Long storeId)
    {
        return ResponseEntity.ok(inventoryService.getLowStockByStoreId(storeId));
    }

    @GetMapping("/{id}/threshold")
    public ResponseEntity<InventoryDto> updateThreshold(@PathVariable Long id,
                                                        @RequestParam Integer threshold) throws Exception
    {
        return ResponseEntity.ok(inventoryService.updateLowStockThreshold(id, threshold));
    }

    @PostMapping("/{id}/add-stock")
    public ResponseEntity<InventoryDto> addStock(@PathVariable Long id,
                                                 @RequestParam Integer quantity) throws Exception
    {
        return ResponseEntity.ok(inventoryService.addStock(id, quantity));
    }
}
