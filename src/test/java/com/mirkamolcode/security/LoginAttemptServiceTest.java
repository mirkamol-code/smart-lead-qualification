package com.mirkamolcode.security;

import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.repository.ApplicationUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LoginAttemptServiceTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @InjectMocks
    private LoginAttemptService underTest;

    private ApplicationUser user;

    @BeforeEach
    void setUp() {
        user = new ApplicationUser("user123", "password", Set.of());
    }

    @Test
    void shouldIncrementFailedAttemptsOnFailure() {
        // given
        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user123", "wrong");
        var event = new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));

        // when
        underTest.onFailure(event);

        // then
        assertThat(user.getNumberOfFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.isAccountLocked()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void shouldSetLockedUntilAndResetCounterWhenMaxAttemptsReached() {
        // given
        user.setNumberOfFailedLoginAttempts(4);
        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user123", "wrong");
        var event = new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));

        // when
        underTest.onFailure(event);

        // then
        assertThat(user.getNumberOfFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLockedUntil()).isNotNull();
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
        assertThat(user.isAccountLocked()).isTrue();
        verify(userRepository).save(user);
    }

    @Test
    void shouldNotRelockOnSingleFailureAfterLockExpires() {
        // given - user was locked (counter reset) and the lock has now expired
        user.setNumberOfFailedLoginAttempts(0);
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));
        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user123", "wrong");
        var event = new AuthenticationFailureBadCredentialsEvent(auth, new BadCredentialsException("bad"));

        // when
        underTest.onFailure(event);

        // then
        assertThat(user.getNumberOfFailedLoginAttempts()).isEqualTo(1);
        assertThat(user.isAccountLocked()).isFalse();
        verify(userRepository).save(user);
    }

    @Test
    void shouldAutomaticallyUnlockWhenLockExpires() {
        // given - locked in the past
        user.setLockedUntil(LocalDateTime.now().minusMinutes(1));

        // then - isAccountLocked should automatically be false
        assertThat(user.isAccountLocked()).isFalse();
    }

    @Test
    void shouldResetFailedAttemptsAndLockOnSuccess() {
        // given
        user.setNumberOfFailedLoginAttempts(5);
        user.setLockedUntil(LocalDateTime.now().plusMinutes(5));
        given(userRepository.findApplicationUserByUsername("user123")).willReturn(Optional.of(user));
        var auth = new UsernamePasswordAuthenticationToken("user123", "correct");
        var event = new AuthenticationSuccessEvent(auth);

        // when
        underTest.onSuccess(event);

        // then
        assertThat(user.getNumberOfFailedLoginAttempts()).isEqualTo(0);
        assertThat(user.getLockedUntil()).isNull();
        assertThat(user.isAccountLocked()).isFalse();
        verify(userRepository).save(user);
    }
}
