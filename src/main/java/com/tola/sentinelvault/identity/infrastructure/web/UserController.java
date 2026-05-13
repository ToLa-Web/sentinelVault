package com.tola.sentinelvault.identity.infrastructure.web;

import com.tola.sentinelvault.identity.application.command.UpdateUserRoleCommand;
import com.tola.sentinelvault.identity.application.dto.UpdateUserRoleRequest;
import com.tola.sentinelvault.identity.application.usecase.UpdateUserRoleUseCase;
import com.tola.sentinelvault.identity.domain.model.Role;
import com.tola.sentinelvault.identity.infrastructure.security.CustomUserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
    @RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserRoleUseCase updateUserRoleUseCase;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser
            ) {
        UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(id, Role.valueOf(request.role().toUpperCase()), currentUser.getId());
        updateUserRoleUseCase.execute(cmd);
        return ResponseEntity.ok().build();
    }
}
