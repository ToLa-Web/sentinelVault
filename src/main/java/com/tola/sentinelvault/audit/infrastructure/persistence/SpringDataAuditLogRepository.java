package com.tola.sentinelvault.audit.infrastructure.persistence;

import com.tola.sentinelvault.audit.domain.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataAuditLogRepository extends JpaRepository<JpaAuditLogEntity, UUID> {

    List<JpaAuditLogEntity> findByActorId(UUID userId);
    List<JpaAuditLogEntity> findByResourceId(UUID resourceId);
}
