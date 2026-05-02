package com.tola.sentinelvault.vault.application.usecase;

import com.tola.sentinelvault.vault.application.dto.SecretResponse;
import com.tola.sentinelvault.vault.application.query.SearchSecretsQuery;
import com.tola.sentinelvault.vault.domain.model.Secret;
import com.tola.sentinelvault.vault.domain.repository.SecretRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchSecretsUseCase {
    private final SecretRepository secretRepository;

    public List<SecretResponse> execute(SearchSecretsQuery query){
        List<Secret> secrets = (query.nameFragment() == null || query.nameFragment().isBlank())
                ? secretRepository.findByOwnerId(query.ownerId())
                : secretRepository.SearchByOwnerAndName(query.ownerId(), query.nameFragment());
        return secrets.stream()
                .map(SecretResponse::from)
                .toList();
    }
}
