package com.renko.repository;

import com.renko.entities.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>
{
//    List<ProductEntity> findByStoreEntyty(Long storeId);

    @Query
    (
            "SELECT p FROM ProductEntity p " +
            "WHERE p.storeEntity.id = :storeId AND (" +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) LIKE LOWER(CONCAT('%', :keyword, '%'))" + ")"
    )
    List<ProductEntity> searchByKeyword(@Param("storeId") Long storeId,
                                        @Param("keyword") String keyword);
}
