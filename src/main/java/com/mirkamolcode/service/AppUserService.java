package com.mirkamolcode.service;

import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.exception.ResourceNotFoundException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AppUserService {
    private final ApplicationUserRepository applicationUserRepository;

    public AppUserService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
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
