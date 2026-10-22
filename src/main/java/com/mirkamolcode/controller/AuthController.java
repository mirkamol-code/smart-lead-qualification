package com.mirkamolcode.controller;

import com.mirkamolcode.dto.ApiResponse;
import com.mirkamolcode.dto.RegisterRequest;
import com.mirkamolcode.dto.UserResponse;
import com.mirkamolcode.service.AppUserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppUserService appUserService;

    public AuthController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = appUserService.register(request);
        return ApiResponse.success("User registered successfully", 201, response);
    }
}
