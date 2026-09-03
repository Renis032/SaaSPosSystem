package com.renko.repository;

public interface OrderItemRepository
{
    @org.springframework.data.jpa.repository.Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.orderEntity.storeEntity.id = :storeId")
    Long sumQuantityByStoreEntity_Id(Long storeId);
}
