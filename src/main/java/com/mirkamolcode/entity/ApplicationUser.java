package com.mirkamolcode.entity;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "app_users")
public class ApplicationUser {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Integer numberOfFailedLoginAttempts = 0;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "app_user_role",
            joinColumns = @JoinColumn(name = "application_user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> appUserRoles = new HashSet<>();

    @Column(nullable = false)
    private boolean isEnabled = true;

    @Column(nullable = false)
    private boolean isAccountLocked = false;

    @Column
    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private boolean isAccountExpired = false;

    @Column(nullable = false)
    private boolean isCredentialsExpired = false;

    public ApplicationUser() {
        this.numberOfFailedLoginAttempts = 0;
    }

    public ApplicationUser(String username, String password, Set<Role> appUserRoles) {
        this.username = username;
        this.password = password;
        this.appUserRoles = appUserRoles;
        this.numberOfFailedLoginAttempts = 0;
    }

    public ApplicationUser(String username, String password, Set<Role> appUserRoles, Integer numberOfFailedLoginAttempts) {
        this.username = username;
        this.password = password;
        this.appUserRoles = appUserRoles;
        this.numberOfFailedLoginAttempts = numberOfFailedLoginAttempts;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getAppUserRoles() {
        return appUserRoles;
    }

    public void setAppUserRoles(Set<Role> appUserRoles) {
        this.appUserRoles = appUserRoles;
    }

    public Integer getNumberOfFailedLoginAttempts() {
        return numberOfFailedLoginAttempts;
    }

    public void setNumberOfFailedLoginAttempts(Integer numberOfFailedLoginAttempts) {
        this.numberOfFailedLoginAttempts = numberOfFailedLoginAttempts;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isAccountLocked() {
        if (isAccountLocked) {
            return true;
        }
        return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
    }

    public void setAccountLocked(boolean accountLocked) {
        isAccountLocked = accountLocked;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public boolean isAccountExpired() {
        return isAccountExpired;
    }

    public void setAccountExpired(boolean accountExpired) {
        isAccountExpired = accountExpired;
    }

    public boolean isCredentialsExpired() {
        return isCredentialsExpired;
    }

    public void setCredentialsExpired(boolean credentialsExpired) {
        isCredentialsExpired = credentialsExpired;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationUser that = (ApplicationUser) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
