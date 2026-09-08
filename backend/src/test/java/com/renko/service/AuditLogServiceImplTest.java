package com.renko.service;

import com.renko.entities.AuditLogEntity;
import com.renko.repository.AuditLogRepository;
import com.renko.service.impl.AuditLogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceImplTest
{
    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private StoreAccessService storeAccessService;

    @InjectMocks
    private AuditLogServiceImpl auditLogService;

    @Test
    void recordPersistsAuditRow()
    {
        when(auditLogRepository.save(any(AuditLogEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        auditLogService.record(5L, "ORDER_CREATE", "Order", "12", "total=10");

        ArgumentCaptor<AuditLogEntity> captor = ArgumentCaptor.forClass(AuditLogEntity.class);
        verify(auditLogRepository).save(captor.capture());
        AuditLogEntity saved = captor.getValue();
        assertEquals(5L, saved.getStoreId());
        assertEquals("ORDER_CREATE", saved.getAction());
        assertEquals("Order", saved.getEntityType());
        assertEquals("12", saved.getEntityId());
    }
}
