package com.algorena.common.config;

import com.algorena.test.config.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;


// Integration test because I want to verify the actual Spring bean
class ObjectMapperConfigIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    record TestDto(LocalDate date, TestEnum status) {
    }

    enum TestEnum {
        ACTIVE, INACTIVE
    }

    @BeforeEach
    void setUp() {
        assertThat(objectMapper).isNotNull();
    }

    @Test
    void serializesDatesAsIsoStrings() throws Exception {
        TestDto dto = new TestDto(LocalDate.of(2025, 11, 7), TestEnum.ACTIVE);
        String json = objectMapper.writeValueAsString(dto);
        // date should be present as ISO string
        assertThat(json).contains("2025-11-07");
    }

    @Test
    void deserializesEnumCaseInsensitive() throws Exception {
        String json = "{\"date\":\"2025-11-07\",\"status\":\"active\"}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);
        assertThat(dto.status()).isEqualTo(TestEnum.ACTIVE);
    }

    @Test
    void emptyStringEnumMapsToNull() throws Exception {
        String json = "{\"date\":\"2025-11-07\",\"status\":\"\"}";
        TestDto dto = objectMapper.readValue(json, TestDto.class);
        assertThat(dto.status()).isNull();
    }

}

