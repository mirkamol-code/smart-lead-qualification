package com.mirkamolcode.security;

import com.mirkamolcode.repository.ApplicationUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginAttemptService {

    private final ApplicationUserRepository applicationUserRepository;
    private static final Logger log = LoggerFactory.getLogger(LoginAttemptService.class);
    private static final int MAX_FAILED_ATTEMPTS = 10;

    public LoginAttemptService(ApplicationUserRepository applicationUserRepository) {
        this.applicationUserRepository = applicationUserRepository;
    }

    @EventListener
    @Transactional
    public void onSuccess(AuthenticationSuccessEvent e) {
        String username = e.getAuthentication().getName();
        applicationUserRepository.findApplicationUserByUsername(username)
                .ifPresent(user -> {
                    if (user.getNumberOfFailedLoginAttempts() != null && user.getNumberOfFailedLoginAttempts() > 0) {
                        user.setNumberOfFailedLoginAttempts(0);
                        applicationUserRepository.save(user);
                        log.info("Reset failed login attempts for user '{}'", username);
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
                            user.setAccountLocked(true);
                            log.warn("User '{}' has been blocked after {} failed login attempts.", username, newAttempts);

                        } else {
                            log.warn("Failed login attempt {}/{} for user '{}'", newAttempts, MAX_FAILED_ATTEMPTS, username);
                        }
                        applicationUserRepository.save(user);
                    }
                });
    }
}
