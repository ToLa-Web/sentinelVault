package com.tola.sentinelvault.identity.infrastructure.web;

import com.tola.sentinelvault.audit.application.command.RecordAuditEventCommand;
import com.tola.sentinelvault.audit.domain.model.AuditActions;
import com.tola.sentinelvault.audit.domain.model.AuditLog;
import com.tola.sentinelvault.audit.domain.port.AuditEventPublisher;
import com.tola.sentinelvault.identity.application.command.ChangePasswordCommand;
import com.tola.sentinelvault.identity.application.command.UpdateUserRoleCommand;
import com.tola.sentinelvault.identity.application.dto.ChangePasswordRequest;
import com.tola.sentinelvault.identity.application.dto.UpdateUserRoleRequest;
import com.tola.sentinelvault.identity.application.usecase.ChangePasswordUseCase;
import com.tola.sentinelvault.identity.application.usecase.UpdateUserRoleUseCase;
import com.tola.sentinelvault.identity.domain.model.Role;
import com.tola.sentinelvault.identity.infrastructure.security.CustomUserPrincipal;
import com.tola.sentinelvault.platform.dto.ApiResponse;
import com.tola.sentinelvault.platform.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UpdateUserRoleUseCase updateUserRoleUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final AuditEventPublisher auditEventPublisher;
    private final ClientIpResolver clientIpResolver;

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUserRole(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRoleRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            HttpServletRequest httpRequest
            ) {
        String clientIp = clientIpResolver.resolve(httpRequest);
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);
        try {
            UpdateUserRoleCommand cmd = new UpdateUserRoleCommand(id, Role.valueOf(request.role().toUpperCase()),
                    currentUser.getId());
            updateUserRoleUseCase.execute(cmd);
            auditEventPublisher.publish(new RecordAuditEventCommand(
                    UUID.randomUUID(), currentUser.getId(), AuditActions.ROLE_UPDATED, "User",
                    id, AuditLog.Outcome.SUCCESS, clientIp, userAgent,
                    "Role updated to " + request.role().toUpperCase() + " for userId=" + id,
                    Instant.now()
            ));
            return ResponseEntity.ok().body(ApiResponse.success("User role updated successfully"));
        } catch (Exception e) {
            auditEventPublisher.publish(new RecordAuditEventCommand(
                    UUID.randomUUID(), currentUser.getId(), AuditActions.ROLE_UPDATED, "User",
                    id, AuditLog.Outcome.FAILURE, clientIp, userAgent,
                    "Role update failed for userId=" + id + ": " + e.getMessage(),
                    Instant.now()
            ));
            throw e;
        }
    }

    @PutMapping("/me/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'MEMBER', 'VIEWER')")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserPrincipal currentUser,
            HttpServletRequest httpRequest
    ) {
        String clientIp  = clientIpResolver.resolve(httpRequest);
        String userAgent = httpRequest.getHeader(HttpHeaders.USER_AGENT);

        try {
            ChangePasswordCommand cmd = new ChangePasswordCommand(currentUser.getId(), request.currentPassword(),
                    request.newPassword()
            );
            changePasswordUseCase.execute(cmd);
            auditEventPublisher.publish(new RecordAuditEventCommand(UUID.randomUUID(),
                    currentUser.getId(),          // actorId  — the user changing their own password
                    AuditActions.PASSWORD_CHANGED,
                    "User",
                    currentUser.getId(),          // resourceId — their own account
                    AuditLog.Outcome.SUCCESS,
                    clientIp,
                    userAgent,
                    "Password changed",           // no detail about old/new password
                    Instant.now()
            ));

            return ResponseEntity.ok().body(ApiResponse.success("Password changed successfully"));
        } catch (Exception e) {
            auditEventPublisher.publish(new RecordAuditEventCommand(
                    UUID.randomUUID(),
                    currentUser.getId(),
                    AuditActions.PASSWORD_CHANGED,
                    "User",
                    currentUser.getId(),
                    AuditLog.Outcome.FAILURE,
                    clientIp,
                    userAgent,
                    "Password change failed: " + e.getMessage(),
                    Instant.now()
            ));
            throw e;
        }
    }
}
