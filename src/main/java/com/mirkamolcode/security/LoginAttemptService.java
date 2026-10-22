package com.mirkamolcode.security;

import com.mirkamolcode.repository.ApplicationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoginAttemptService {

    private final ApplicationUserRepository applicationUserRepository;
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_FAILED_ATTEMPTS = 10;
    private static final int LOCK_DURATION_MINUTES = 15;

    public LoginAttemptService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent e) {
        String username = e.getAuthentication().getName();
        applicationUserRepository.findApplicationUserByUsername(username)
                .ifPresent(user -> {
                    boolean updated = false;
                    if (user.getNumberOfFailedLoginAttempts() != null && user.getNumberOfFailedLoginAttempts() > 0) {
                        user.setNumberOfFailedLoginAttempts(0);
                        updated = true;
                        log.info("Reset failed login attempts for user '{}'", username);
                    }
                    if (user.getLockedUntil() != null) {
                        user.setLockedUntil(null);
                        updated = true;
                    }
                    if (updated) {
                        applicationUserRepository.save(user);
                    }
                });
    }


    @EventListener
    @Transactional
    public void onFailure(AuthenticationFailureBadCredentialsEvent e) {
        String username = e.getAuthentication().getName();
        applicationUserRepository.findApplicationUserByUsername(username)
                .ifPresent(user -> {
                    if (!user.isAccountLocked()) {
                        int currentAttempts = user.getNumberOfFailedLoginAttempts() != null
                                ? user.getNumberOfFailedLoginAttempts() : 0;
                        int newAttempts = currentAttempts + 1;
                        user.setNumberOfFailedLoginAttempts(newAttempts);

                        if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                            log.warn("User '{}' has been temporarily locked for {} minutes after {} failed login attempts.",
                                    username, LOCK_DURATION_MINUTES, newAttempts);
                        } else {
                            log.warn("Failed login attempt {}/{} for user '{}'", newAttempts, MAX_FAILED_ATTEMPTS, username);
                        }
                        applicationUserRepository.save(user);
                    }
                });
    }
}
