package com.tola.sentinelvault.identity.infrastructure.persistence;
import com.tola.sentinelvault.identity.domain.model.Email;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {
    private final SpringDataUserRepository springDataUserRepository;
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
        springDataUserRepository.save(entity);
        return user;
    }
    @Override
    public Optional<User> findById(UUID id) {
        return springDataUserRepository.findById(id)
                .map(this::mapToDomain);
    }
    @Override
    public Optional<User> findByEmail(Email email) {
        return springDataUserRepository.findByEmail(email.value())
                .map(this::mapToDomain);
    }
    @Override
    public boolean existsByEmail(Email email) {
        return springDataUserRepository.existsByEmail(email.value());
    }
    private User mapToDomain(JpaUserEntity entity) {
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