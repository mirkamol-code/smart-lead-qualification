package com.mirkamolcode.service;

import com.mirkamolcode.dto.RegisterRequest;
import com.mirkamolcode.dto.UserResponse;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Role;
import com.mirkamolcode.exception.DuplicateResourceException;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.RoleRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AppUserService {
    private final ApplicationUserRepository applicationUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(ApplicationUserRepository applicationUserRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder) {
        this.applicationUserRepository = applicationUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (applicationUserRepository.findApplicationUserByUsername(request.username()).isPresent()) {
            throw new DuplicateResourceException("Username '" + request.username() + "' is already taken");
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("USER role not found in database"));

        ApplicationUser user = new ApplicationUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                Set.of(userRole)
        );

        ApplicationUser savedUser = applicationUserRepository.save(user);

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getAppUserRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }

    @Transactional
    public void unlockUser(UUID id) {
        ApplicationUser user = applicationUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setAccountLocked(false);
        user.setLockedUntil(null);
        user.setNumberOfFailedLoginAttempts(0);
        applicationUserRepository.save(user);
    }

    @Transactional
    public void unlockUserByUsername(String username) {
        ApplicationUser user = applicationUserRepository.findApplicationUserByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));

        user.setAccountLocked(false);
        user.setLockedUntil(null);
        user.setNumberOfFailedLoginAttempts(0);
        applicationUserRepository.save(user);
    }
}
