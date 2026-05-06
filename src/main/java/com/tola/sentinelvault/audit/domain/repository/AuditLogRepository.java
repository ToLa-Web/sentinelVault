package com.tola.sentinelvault.audit.domain.repository;

import com.tola.sentinelvault.audit.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {

    AuditLog save(AuditLog auditLog);
    List<AuditLog> findByActorId(UUID actorId);
    List<AuditLog> findByResourceId(UUID resourceId);
}
