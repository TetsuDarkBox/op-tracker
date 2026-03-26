package com.optracker.api.dto;

import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String username,
        String email,
        String displayName,
        String city,
        String country,
        Integer positiveEvaluations,
        LocalDateTime memberSince
) {
    // Record para uma resposta limpa e segura
}