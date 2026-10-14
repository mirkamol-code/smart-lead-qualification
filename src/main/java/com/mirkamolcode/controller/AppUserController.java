package com.mirkamolcode.controller;

import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.service.AppUserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unlockUserById(@PathVariable UUID id) {
        appUserService.unlockUser(id);
        return ApiResponse.success("User with id " + id + " has been successfully unlocked.");
    }

    @PatchMapping("/username/{username}/unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> unlockUserByUsername(@PathVariable String username) {
        appUserService.unlockUserByUsername(username);
        return ApiResponse.success("User " + username + " has been successfully unlocked.");
    }
}
