package com.mirkamolcode.dto;

import java.util.Set;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        Set<String> roles
) {
}
