package com.furniture.dto;

import java.time.LocalDateTime;

public record ClientAccessResponse(
        Long id,
        Long projectId,
        LocalDateTime expiresAt,
        Boolean active,
        LocalDateTime createdAt
) {
}
