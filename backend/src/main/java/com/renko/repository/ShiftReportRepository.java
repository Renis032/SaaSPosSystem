package com.renko.repository;

import com.renko.entities.ShiftReportEntity;
import com.renko.entities.UserEntity;
import com.renko.payload.dto.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftReportRepository extends JpaRepository<ShiftReportEntity, Long>
{
    List<ShiftReportEntity> findByCashierEntity_Id(Long id);
    List<ShiftReportEntity> findByStoreEntity_Id(Long storeId);

    Optional<ShiftReportEntity> findTopByCashierEntityAndShiftEndIsNullOrderByShiftStartDesc(UserDto cashier);

    Optional<ShiftReportEntity> findByCashierEntityAndShiftStartBetween(UserEntity cashier,
                                                                  LocalDateTime start,
                                                                  LocalDateTime end);
}
