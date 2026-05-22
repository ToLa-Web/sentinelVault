package com.tola.sentinelvault.audit.infrastructure.persistence;

import com.tola.sentinelvault.audit.domain.model.AuditLog;
import com.tola.sentinelvault.audit.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final SpringDataAuditLogRepository springRepo;

    @Override
    public AuditLog save(AuditLog log) {
        springRepo.save(toEntity(log));
        return log;
    }

    @Override
    public List<AuditLog> findByActorId(UUID actorId) {
        return springRepo.findByActorId(actorId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public List<AuditLog> findByResourceId(UUID resourceId) {
        return springRepo.findByResourceId(resourceId)
                .stream().map(this::toDomain).toList();
    }

    private JpaAuditLogEntity toEntity(AuditLog log) {
        return JpaAuditLogEntity.builder()
                .id(log.getId())
                .actorId(log.getActorId())
                .action(log.getAction())
                .resourceType(log.getResourceType())
                .resourceId(log.getResourceId())
                .outcome(log.getOutcome())
                .clientIp(log.getClientIp())
                .userAgent(log.getUserAgent())
                .detail(log.getDetail())
                .occurredOn(log.getOccurredOn())
                .build();
    }

    private AuditLog toDomain(JpaAuditLogEntity entity) {
        return AuditLog.reconstitute(
                entity.getId(),
                entity.getActorId(),
                entity.getAction(),
                entity.getResourceType(),
                entity.getResourceId(),
                entity.getOutcome(),
                entity.getClientIp(),
                entity.getUserAgent(),
                entity.getDetail(),
                entity.getOccurredOn()
        );
    }
}
