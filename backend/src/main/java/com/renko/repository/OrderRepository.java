package com.renko.repository;

import com.renko.entities.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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

    @Query("SELECT SUM(o.totalAmount) FROM OrderEntity o WHERE o.storeEntity.id = :storeId")
    Double sumTotalAmountByStoreId(@Param("storeId") Long storeId);

    @Query("""
            SELECT DISTINCT o FROM OrderEntity o
            LEFT JOIN FETCH o.items i
            LEFT JOIN FETCH i.productEntity
            LEFT JOIN FETCH o.storeEntity
            LEFT JOIN FETCH o.customerEntity
            LEFT JOIN FETCH o.cashierEntity
            WHERE o.id = :id
            """)
    Optional<OrderEntity> findDetailedById(@Param("id") Long id);
}
