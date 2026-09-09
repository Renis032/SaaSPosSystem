package com.renko.service;

public interface AuditLogService
{
    void record(Long storeId, String action, String entityType, String entityId, String details);

    void recordChange(Long storeId,
                      String action,
                      String entityType,
                      String entityId,
                      String beforeState,
                      String afterState,
                      String details);

    java.util.List<com.renko.entities.AuditLogEntity> listForStore(Long storeId) throws Exception;
}
