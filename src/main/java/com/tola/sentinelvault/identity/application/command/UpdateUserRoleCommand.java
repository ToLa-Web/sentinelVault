package com.tola.sentinelvault.identity.application.command;

import com.tola.sentinelvault.identity.domain.model.Role;

import java.util.UUID;

public record UpdateUserRoleCommand(
        UUID targetUserId,
        Role newRole,
        UUID adminId
) {
}
