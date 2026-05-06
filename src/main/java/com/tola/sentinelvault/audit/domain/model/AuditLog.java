package com.tola.sentinelvault.audit.domain.model;

import com.tola.sentinelvault.shared.domain.base.AggregateRoot;

import java.time.Instant;
import java.util.UUID;
/**
 * Aggregate Root for the Audit bounded context.
 *
 * An AuditLog is an immutable record of something that happened
 * in the system. Once created it is never mutated.
 *
 * Invariants:
 *  - actorId is always present (the user who triggered the action)
 *  - action is always non-blank
 *  - occurredOn is always set at creation time
 */
public class AuditLog extends AggregateRoot {
    private final UUID actorId;
    private final String action;
    private final String resourceType;
    private final UUID resourceId;
    private final Instant occurredOn;

    private AuditLog(UUID id, UUID actorId,String action, String resourceType, UUID resourceId, Instant occurredOn) {
        super(id);
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.occurredOn = occurredOn;
    }

    public static AuditLog record(UUID actorId, String action, String resourceType, UUID resourceId, Instant occurredOn) {
        if (actorId == null) throw new IllegalArgumentException("Actor ID cannot be null");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("Action cannot be null");

        return new AuditLog(UUID.randomUUID(), actorId, action, resourceType, resourceId, occurredOn);
    }

    public static AuditLog reconstitute(UUID id, UUID actorId, String action, String resourceType, UUID resourceId, Instant occurredOn) {
        return new AuditLog(id, actorId, action, resourceType, resourceId, occurredOn);
    }

    public UUID getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getResourceType() { return resourceType; }
    public UUID getResourceId() { return resourceId; }
    public Instant getOccurredOn() { return occurredOn; }
}
