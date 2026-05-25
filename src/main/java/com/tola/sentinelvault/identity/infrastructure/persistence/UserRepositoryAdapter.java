package com.tola.sentinelvault.identity.infrastructure.persistence;
import com.tola.sentinelvault.identity.domain.model.Email;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;
    private final EntityManager entityManager;

    @Override
    public User save(User user) {
        JpaUserEntity entity = new JpaUserEntity(
                user.getId(),
                user.getEmail().value(),
                user.getPasswordHash(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        entityManager.merge(entity);
        entityManager.flush();

        return user;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.value())
                .map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return springDataUserRepository.existsByEmail(email.value());
    }

    private User toDomain(JpaUserEntity entity) {
        return User.reconstitute(
                entity.getId(),
                Email.of(entity.getEmail()),
                entity.getPasswordHash(),
                entity.getRole(),
                entity.isEnabled(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}