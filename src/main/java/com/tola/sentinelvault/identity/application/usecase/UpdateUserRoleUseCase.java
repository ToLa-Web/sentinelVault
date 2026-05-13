package com.tola.sentinelvault.identity.application.usecase;

import com.tola.sentinelvault.identity.application.command.UpdateUserRoleCommand;
import com.tola.sentinelvault.identity.domain.model.User;
import com.tola.sentinelvault.identity.domain.repository.UserRepository;
import com.tola.sentinelvault.shared.domain.exception.DomainException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UpdateUserRoleUseCase {
    private final UserRepository userRepository;

    @Transactional
    public void execute(UpdateUserRoleCommand cmd){
        User user = userRepository.findById(cmd.targetUserId())
                .orElseThrow(UserNotFoundException::new);

        if (cmd.targetUserId().equals(cmd.adminId())) {
            throw new UserChangeRoleException();
        }
        user.changeRole(cmd.newRole(), cmd.adminId());
        userRepository.save(user);
    }

    public static class UserChangeRoleException extends DomainException {
        public UserChangeRoleException() {
            super("Admins cannot change their own roles to prevent lockouts.");
        }
    }
    public static class UserNotFoundException extends DomainException {
        public UserNotFoundException() {
            super("User not found");
        }
    }

}
