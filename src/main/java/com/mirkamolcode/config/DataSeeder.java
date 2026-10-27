package com.mirkamolcode.config;

import com.mirkamolcode.entity.ApplicationUser;
import com.mirkamolcode.entity.Permission;
import com.mirkamolcode.entity.Role;
import com.mirkamolcode.repository.ApplicationUserRepository;
import com.mirkamolcode.repository.PermissionRepository;
import com.mirkamolcode.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Component
@ConditionalOnProperty(
        name = "app.data-seeder.enabled",
        havingValue = "true",
        matchIfMissing = false)
public class DataSeeder implements CommandLineRunner {
    private final ApplicationUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.admin.username:admin123}")
    private String adminUsername;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password:password}")
    private String adminPassword;

    @org.springframework.beans.factory.annotation.Value("${spring.profiles.active:local}")
    private String activeProfile;

    public DataSeeder(ApplicationUserRepository userRepository, RoleRepository roleRepository, PermissionRepository permissionRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Permission anyCommentRead = getOrCreatePermission("any comment:read");
        Permission ownCommentRead = getOrCreatePermission("own comment:read");
        Permission commentListRead = getOrCreatePermission("commentList:read");
        Permission commentCreate = getOrCreatePermission("comment:create"); // No trailing space!
        Permission ticketListRead = getOrCreatePermission("ticketList:read");
        Permission ticketsRead = getOrCreatePermission("tickets:read");

        Role userRole = getOrCreateRole("USER", Set.of(ownCommentRead, commentCreate));
        Role adminRole = getOrCreateRole("ADMIN", Set.of(ownCommentRead, commentCreate, anyCommentRead, commentListRead, ticketListRead, ticketsRead));

        createUserIfNotFound(adminUsername, adminPassword, Set.of(adminRole));

        if (!"prod".equalsIgnoreCase(activeProfile)) {
            createUserIfNotFound("user123", "password", Set.of(userRole));
        }
    }

    private Permission getOrCreatePermission(String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(new Permission(name)));
    }

    private Role getOrCreateRole(String name, Set<Permission> permissions) {
        return roleRepository.findByName(name)
                .map(existingRole -> {
                    existingRole.getPermissions().addAll(permissions);
                    return roleRepository.save(existingRole);
                })
                .orElseGet(() -> roleRepository.save(new Role(name, new HashSet<>(permissions))));
    }

    private void createUserIfNotFound(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.findApplicationUserByUsername(username).isEmpty()) {
            ApplicationUser user = new ApplicationUser(
                    username,
                    passwordEncoder.encode(rawPassword),
                    new HashSet<>(roles)
            );
            userRepository.save(user);
        }
    }
}

