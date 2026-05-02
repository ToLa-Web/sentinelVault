package com.tola.sentinelvault.vault.infrastructure.web;

import com.tola.sentinelvault.vault.application.command.CreateSecretCommand;
import com.tola.sentinelvault.vault.application.command.UpdateSecretCommand;
import com.tola.sentinelvault.vault.application.dto.CreateSecretRequest;
import com.tola.sentinelvault.vault.application.dto.SecretDetailResponse;
import com.tola.sentinelvault.vault.application.dto.SecretResponse;
import com.tola.sentinelvault.vault.application.dto.UpdateSecretRequest;
import com.tola.sentinelvault.vault.application.query.SearchSecretsQuery;
import com.tola.sentinelvault.vault.application.usecase.CreateSecretUseCase;
import com.tola.sentinelvault.vault.application.usecase.GetSecretUseCase;
import com.tola.sentinelvault.vault.application.usecase.SearchSecretsUseCase;
import com.tola.sentinelvault.vault.application.usecase.UpdateSecretUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/secrets")
@RequiredArgsConstructor
public class SecretController {

    private final CreateSecretUseCase createSecretUseCase;
    private final UpdateSecretUseCase updateSecretUseCase;
    private final GetSecretUseCase getSecretUseCase;
    private final SearchSecretsUseCase searchSecretsUseCase;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<SecretResponse> create(
            @Valid @RequestBody CreateSecretRequest request,
            @AuthenticationPrincipal String userId) {
        CreateSecretCommand cmd = new CreateSecretCommand(request.name(), request.value(), UUID.fromString(userId));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createSecretUseCase.execute(cmd));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<SecretResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSecretRequest request,
            @AuthenticationPrincipal String userId
    ) {
        UpdateSecretCommand cmd = new UpdateSecretCommand(id, request.name(), request.value(), UUID.fromString(userId));
        return ResponseEntity.ok(updateSecretUseCase.execute(cmd));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER', 'VIEWER')")
    public ResponseEntity<SecretDetailResponse> get(
            @PathVariable UUID id,
            @AuthenticationPrincipal String userId) {

        return ResponseEntity.ok(getSecretUseCase.execute(id, UUID.fromString(userId)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER','VIEWER')")
    public ResponseEntity<List<SecretResponse>> search(
            @RequestParam(required = false) String name,
            @AuthenticationPrincipal String userId) {

        SearchSecretsQuery query = new SearchSecretsQuery(UUID.fromString(userId), name);
        return ResponseEntity.ok(searchSecretsUseCase.execute(query));
    }
}
