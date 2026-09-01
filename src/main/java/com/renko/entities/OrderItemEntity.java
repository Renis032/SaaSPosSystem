package com.renko.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class OrderItemEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Integer quantity;

    private Double price;
    private Double originalPrice;
    private Double discountApplied = 0.0;

    @ManyToOne
    private ProductEntity productEntity;

    @ManyToOne
    private OrderEntity orderEntity;
}
