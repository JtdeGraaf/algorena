package com.algorena.bots.domain;

import com.algorena.security.EncryptionService;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * JPA AttributeConverter that automatically encrypts API keys before storing them in the database
 * and decrypts them when reading from the database.
 * <p>
 * This converter is applied to fields annotated with @Convert(converter = ApiKeyConverter.class).
 */
@Converter
@Component
@AllArgsConstructor
public class ApiKeyConverter implements AttributeConverter<String, String> {

    private final EncryptionService encryptionService;

    @Override
    @Nullable
    public String convertToDatabaseColumn(@Nullable String attribute) {
        return encryptionService.encrypt(attribute);
    }

    @Override
    @Nullable
    public String convertToEntityAttribute(@Nullable String dbData) {
        return encryptionService.decrypt(dbData);
    }
}
