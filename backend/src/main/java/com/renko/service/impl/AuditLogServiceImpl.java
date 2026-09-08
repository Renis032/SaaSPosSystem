package com.renko.service.impl;

import com.renko.entities.AuditLogEntity;
import com.renko.payload.dto.UserDto;
import com.renko.repository.AuditLogRepository;
import com.renko.service.AuditLogService;
import com.renko.service.StoreAccessService;
import com.renko.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService
{
    private final AuditLogRepository auditLogRepository;
    private final UserService userService;
    private final StoreAccessService storeAccessService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(Long storeId, String action, String entityType, String entityId, String details)
    {
        try
        {
            Long actorId = null;
            String actorEmail = null;
            try
            {
                UserDto user = userService.getCurrentUser();
                actorId = user.getId();
                actorEmail = user.getEmail();
            }
            catch(Exception ignored)
            {
                // system / unauthenticated path
            }

            auditLogRepository.save(AuditLogEntity.builder()
                    .storeId(storeId)
                    .actorUserId(actorId)
                    .actorEmail(actorEmail)
                    .action(action)
                    .entityType(entityType)
                    .entityId(entityId)
                    .details(details)
                    .build());
        }
        catch(Exception e)
        {
            log.warn("Failed to write audit log action={} entityType={}: {}", action, entityType, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogEntity> listForStore(Long storeId) throws Exception
    {
        storeAccessService.requireStoreAccess(storeId);
        return auditLogRepository.findTop100ByStoreIdOrderByCreatedAtDesc(storeId);
    }
}
