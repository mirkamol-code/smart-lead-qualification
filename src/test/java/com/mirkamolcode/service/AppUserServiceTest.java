package com.mirkamolcode.service;

import com.mirkamolcode.dto.RegisterRequest;
import com.mirkamolcode.dto.UserResponse;
import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Role;
import com.mirkamolcode.exception.DuplicateResourceException;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    private ApplicationUserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AppUserService underTest;

    @Test
    void shouldRegisterNewUserSuccessfully() {
        // given
        RegisterRequest request = new RegisterRequest("newuser", "securePassword123");
        Role userRole = new Role("USER", Set.of());
        ApplicationUser savedUser = new ApplicationUser("newuser", "encodedHash", Set.of(userRole));
        savedUser.setId(UUID.randomUUID());

        given(userRepository.findApplicationUserByUsername("newuser")).willReturn(Optional.empty());
        given(roleRepository.findByName("USER")).willReturn(Optional.of(userRole));
        given(passwordEncoder.encode("securePassword123")).willReturn("encodedHash");
        given(userRepository.save(any(ApplicationUser.class))).willReturn(savedUser);

        // when
        UserResponse response = underTest.register(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.username()).isEqualTo("newuser");
        assertThat(response.roles()).containsExactly("USER");
        verify(userRepository).save(any(ApplicationUser.class));
    }

    @Test
    void shouldThrowDuplicateResourceExceptionWhenUsernameAlreadyExists() {
        // given
        RegisterRequest request = new RegisterRequest("existinguser", "securePassword123");
        ApplicationUser existingUser = new ApplicationUser("existinguser", "hash", Set.of());

        given(userRepository.findApplicationUserByUsername("existinguser")).willReturn(Optional.of(existingUser));

        // then
        assertThatThrownBy(() -> underTest.register(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username 'existinguser' is already taken");
    }

    @Test
    void shouldUnlockUserById() {
        // given
        UUID id = UUID.randomUUID();
        ApplicationUser lockedUser = new ApplicationUser("user", "pass", Set.of());
        lockedUser.setAccountLocked(true);
        lockedUser.setNumberOfFailedLoginAttempts(5);

        given(userRepository.findById(id)).willReturn(Optional.of(lockedUser));

        // when
        underTest.unlockUser(id);

        // then
        assertThat(lockedUser.isAccountLocked()).isFalse();
        assertThat(lockedUser.getNumberOfFailedLoginAttempts()).isEqualTo(0);
        assertThat(lockedUser.getLockedUntil()).isNull();
        verify(userRepository).save(lockedUser);
    }
}
