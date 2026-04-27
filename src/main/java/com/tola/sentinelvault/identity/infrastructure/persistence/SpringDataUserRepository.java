package com.tola.sentinelvault.identity.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataUserRepository extends JpaRepository<JpaUserEntity, Integer> {
    Optional<JpaUserEntity> findByEmail(String email);
    boolean existsByEmail(String email);
}
