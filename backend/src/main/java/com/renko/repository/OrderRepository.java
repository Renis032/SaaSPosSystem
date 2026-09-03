package com.renko.repository;

import com.renko.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Long>
{
    List<OrderEntity> findByCustomerEntity_Id(Long customerId);
    List<OrderEntity> findByStoreEntity_Id(Long storeId);
    List<OrderEntity> findByStoreEntity_IdOrderByCreatedAtDesc(Long storeId);
    List<OrderEntity> findByCashierEntity_Id(Long cashierId);
    List<OrderEntity> findByStoreEntity_IdAndCreatedAtBetween(Long storeId, LocalDateTime from, LocalDateTime to);
    List<OrderEntity> findByCashierEntity_IdAndCreatedAtBetween(Long cashierId, LocalDateTime from, LocalDateTime to);
    List<OrderEntity> findTopFiveByStoreEntity_IdOrderByCreatedAtDesc(Long storeId);

    long countByStoreEntity_Id(Long storeId);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.storeEntity.id = :storeId")
    Double sumTotalAmountByStoreId(@org.springframework.data.repository.query.Param("storeId") Long storeId);
}
