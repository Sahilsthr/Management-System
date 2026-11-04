package com.furniture.dto;

import java.time.LocalDateTime;

public record ClientAccessCreateResponse(
        Long id,
        Long projectId,
        String token,
        LocalDateTime expiresAt,
        Boolean active,
        LocalDateTime createdAt
) {
}