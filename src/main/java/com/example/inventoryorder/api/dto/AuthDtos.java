package com.example.inventoryorder.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record RegisterRequest(@NotBlank String username, @NotBlank @Size(min = 6) String password) {
    }

    public record TokenResponse(String token) {
    }
}
