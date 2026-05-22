package com.tola.sentinelvault.audit.infrastructure.persistence;

import com.tola.sentinelvault.audit.domain.model.AuditLog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JpaAuditLogEntity {
    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "actor_id", nullable = false)
    private UUID actorId;

    @Column(nullable = false)
    private String action;

    @Column(name = "resource_type")
    private String resourceType;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditLog.Outcome outcome;

    @Column(name = "client_ip", length = 45)       // 45 = max IPv6 length
    private String clientIp;

    @Column(name = "user_agent", length = 200)
    private String userAgent;

    @Column(length = 500)
    private String detail;

    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;
}
