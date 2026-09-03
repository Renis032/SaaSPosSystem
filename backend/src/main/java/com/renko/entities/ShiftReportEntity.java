package com.renko.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Ref;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShiftReportEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private LocalDateTime shiftStart;
    private LocalDateTime shiftEnd;

    private Double totalSales;
    private Double totalRefunds;
    private Double netSales;
    private double totalOrders;

    @ManyToOne
    @JoinColumn(name = "cashier_id", nullable = false)
    private UserEntity cashierEntity;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private StoreEntity storeEntity;

    @Transient
    private List<PaymentSummaryEntity> paymentSummaries;

    @ManyToMany(cascade = CascadeType.MERGE)
    private List<ProductEntity> topSellingProducts;

    @ManyToMany(cascade = CascadeType.MERGE)
    private List<OrderEntity> recentOrders;

    @OneToMany(mappedBy = "shiftReportEntity", cascade = CascadeType.ALL)
    private List<RefundEntity> refunds;
}
