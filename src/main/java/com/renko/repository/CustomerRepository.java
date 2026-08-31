package com.renko.repository;

import com.renko.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long>
{
    List<CustomerEntity> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fistName, String email);
    List<CustomerEntity> findByStoreEntity_Id(Long storeId);
    long countByStoreEntity_Id(Long storeId);
}
