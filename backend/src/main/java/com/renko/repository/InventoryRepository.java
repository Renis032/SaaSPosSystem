package com.renko.repository;

import com.renko.entities.InventoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InventoryRepository extends JpaRepository<InventoryEntity, Long>
{
    InventoryEntity findByStoreEntity_IdAndProductEntity_Id(Long storeId, Long productId);

    List<InventoryEntity> findByStoreEntity_Id(Long id);

    @Query("SELECT i FROM InventoryEntity i WHERE i.storeEntity.id = :storeId AND i.quantity <= i.lowStockThreshold")
    List<InventoryEntity> findLowStockByStore_Id(@Param("storeId") Long storeId);
}
