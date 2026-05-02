package com.tola.sentinelvault.vault.config;

import org.springframework.context.annotation.Configuration;

/**
 * Vault bounded context configuration.
 * Beans are currently wired via @Component scanning.
 * Add explicit @Bean definitions here if manual wiring is needed later
 * (e.g. swapping AES for a KMS-backed encryption service).
 */
@Configuration
public class VaultConfig {
}
