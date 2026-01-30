package com.algorena.bots.dto;

public record CreateApiKeyResponse(
        ApiKeyDTO apiKey,
        String plainTextKey
) {
}

