package com.tola.sentinelvault.identity.domain.model;

import com.tola.sentinelvault.shared.domain.base.AggregateRoot;

import java.time.Instant;
import java.util.UUID;

public class User extends AggregateRoot {

    private Email email;
    private String passwordHash;
    private Role role;
    private boolean enabled;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(UUID id, Email email, String passwordHash, Role role, boolean enabled, Instant createdAt, Instant updatedAt) {
        super(id);
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static User register(Email email, String passwordHash, Role role) {
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }

        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        User user = new User(id, email, passwordHash, role, true, now, now);
        user.registerEvent(new UserRegisteredEvent(UUID.randomUUID(), now, id, email.value()));
        return user;
    }

    public static User reconstitute(UUID id, Email email, String passwordHash, Role role, boolean enabled, Instant createdAt, Instant updatedAt) {
        return new User(id, email, passwordHash, role, enabled, createdAt, updatedAt);
    }

    public void disable() {
        this.enabled = false;
        this.updatedAt = Instant.now();
    }

    public void changeRole(Role newRole, UUID updatedBy) {
        Role oldRole = this.role;
        this.role = newRole;
        this.updatedAt = Instant.now();
        this.registerEvent(new UserRoleChangedEvent(
                UUID.randomUUID(),
                updatedAt,
                oldRole,
                newRole,
                updatedBy
        ));
    }

    public void changePassword(String newPasswordHash) {
        if (newPasswordHash == null || newPasswordHash.isBlank()) {
            throw new IllegalArgumentException("Password hash must not be blank");
        }

        String oldHash = this.passwordHash;
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
        this.registerEvent(new PasswordChangedEvent(
                UUID.randomUUID(),
                updatedAt,
                this.getId(),
                oldHash
        ));
    }

    public Email getEmail() { return email; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
