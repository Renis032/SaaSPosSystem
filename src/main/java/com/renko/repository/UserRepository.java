package com.renko.repository;

import com.renko.domain.UserRole;
import com.renko.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long>
{
    UserEntity findByEmail(String email);
    Optional<UserEntity> findByRole(UserRole role);
    boolean existsByRole(UserRole role);
}
