package com.renko.service;

public interface AuditLogService
{
    void record(Long storeId, String action, String entityType, String entityId, String details);

    java.util.List<com.renko.entities.AuditLogEntity> listForStore(Long storeId) throws Exception;
}
