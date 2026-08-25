package com.renko.model;

import com.renko.domain.StoreStatus;
import com.renko.mapper.UserMapper;
import com.renko.payload.dto.StoreDto;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class StoreEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String brandName;

    @OneToOne
    private UserEntity storeAdmin;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String description;

    private String storeType;

    private StoreStatus status;

    @Embedded
    private StoreContactEntity contact;

    @PrePersist
    protected void onCreate()
    {
        createdAt = LocalDateTime.now();
        status = StoreStatus.PENDING;
    }

    @PreUpdate
    protected void onUpdate()
    {
        updatedAt = LocalDateTime.now();
    }
}
