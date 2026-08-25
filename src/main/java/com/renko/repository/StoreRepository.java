package com.renko.repository;

import com.renko.model.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreRepository extends JpaRepository<StoreEntity, Long>
{
    StoreEntity findByStoreAdmin(Long adminId);

}
